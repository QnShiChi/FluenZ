package com.fluenz.api.service;

import com.fluenz.api.dto.request.OnboardingRequest;
import com.fluenz.api.dto.response.*;
import com.fluenz.api.entity.*;
import com.fluenz.api.entity.enums.LearningMode;
import com.fluenz.api.entity.enums.Level;
import com.fluenz.api.entity.enums.PathStatus;
import com.fluenz.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private static final int TOPIC_BATCH_SIZE = 2;

    private final UserRepository userRepository;
    private final ProfessionRepository professionRepository;
    private final LearningPathRepository learningPathRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final UserSubPhraseProgressRepository progressRepository;
    private final UserChunkProgressRepository chunkProgressRepository;
    private final LlmService llmService;
    private final ImagePopulationService imagePopulationService;
    private final GenerationProgressService generationProgressService;
    @Qualifier("onboardingGenerationExecutor")
    private final Executor onboardingGenerationExecutor;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public GenerationProgressResponse startGeneration(String email, OnboardingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<LearningPath> existingGenerating = learningPathRepository
                .findFirstByUserAndStatusOrderByCreatedAtDesc(user, PathStatus.GENERATING);
        if (existingGenerating.isPresent()) {
            GenerationProgressResponse current = generationProgressService.get(existingGenerating.get().getId());
            if (current != null) {
                return current;
            }
            return GenerationProgressResponse.builder()
                    .pathId(existingGenerating.get().getId())
                    .phase("QUEUED")
                    .statusText("A personalized generation job is already running.")
                    .progressPercent(5)
                    .complete(false)
                    .failed(false)
                    .currentBatch(0)
                    .totalBatches(0)
                    .completedTopics(0)
                    .totalTopics(0)
                    .build();
        }

        Profession profession = null;
        if (request.getProfessionId() != null) {
            profession = professionRepository.findById(request.getProfessionId())
                    .orElseThrow(() -> new RuntimeException("Profession not found"));
        }
        if (profession == null) {
            profession = resolveProfessionFromProfile(request);
        }

        Level effectiveLevel = request.getLevel();
        LearnerProfile learnerProfile = createLearnerProfile(request, profession, effectiveLevel);
        String pathLabel = resolvePathLabel(profession, learnerProfile);
        LearningPath path = LearningPath.builder()
                .title("Generating " + pathLabel + " Communication Path")
                .status(PathStatus.GENERATING)
                .user(user)
                .profession(profession)
                .learnerProfile(learnerProfile)
                .build();
        LearningPath saved = learningPathRepository.save(path);

        GenerationProgressResponse response = generationProgressService.start(
                saved.getId(),
                "Queued personalized generation for " + pathLabel + "."
        );

        UUID pathId = saved.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                onboardingGenerationExecutor.execute(() -> runBackgroundGenerationSafely(pathId, request));
            }
        });

        return response;
    }

    public void runBackgroundGenerationSafely(UUID pathId, OnboardingRequest request) {
        try {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.executeWithoutResult(status -> runBackgroundGeneration(pathId, request));
            generationProgressService.complete(pathId, "Personalized path is ready.");
        } catch (Exception e) {
            log.error("Background onboarding generation failed for path {}: {}", pathId, e.getMessage(), e);
            markGenerationFailed(pathId, e.getMessage());
            generationProgressService.fail(pathId, e.getMessage());
        }
    }

    @Transactional
    public void runBackgroundGeneration(UUID pathId, OnboardingRequest request) {
        LearningPath path = learningPathRepository.findWithGenerationContextById(pathId)
                .orElseThrow(() -> new RuntimeException("Learning path not found"));

        User user = path.getUser();
        Profession profession = path.getProfession();
        LearnerProfile learnerProfile = path.getLearnerProfile();
        Level effectiveLevel = learnerProfile != null && learnerProfile.getLevel() != null
                ? learnerProfile.getLevel()
                : request.getLevel();
        String promptProfession = buildPromptProfession(profession, learnerProfile, request);
        List<String> promptContexts = buildPromptContexts(request);
        String promptGoals = buildPromptGoals(request);
        List<String> communicationPriorities = collectCommunicationPriorities(request);
        String personaSummary = blankToNull(request.getPersonaSummary());
        if (personaSummary == null) {
            personaSummary = learnerProfile != null ? blankToNull(learnerProfile.getPersonaSummary()) : null;
        }
        if (personaSummary == null) {
            personaSummary = buildPersonaSummary(request, resolvePathLabel(profession, learnerProfile), communicationPriorities);
        }

        generationProgressService.markBlueprint(pathId, "Generating learner blueprint.");
        LlmService.LlmBlueprint blueprint = llmService.generateBlueprint(
                promptProfession,
                effectiveLevel.name(),
                promptContexts,
                promptGoals,
                personaSummary
        );

        if (learnerProfile != null && blankToNull(blueprint.getPersonaSummary()) != null) {
            learnerProfile.setPersonaSummary(blueprint.getPersonaSummary().trim());
            learnerProfileRepository.save(learnerProfile);
        }

        List<LlmService.LlmBlueprintTopic> blueprintTopics = blueprint.getTopics();
        if (blueprintTopics == null || blueprintTopics.isEmpty()) {
            throw new RuntimeException("Blueprint generation returned no topics");
        }

        int totalTopics = blueprintTopics.size();
        int totalBatches = (int) Math.ceil((double) totalTopics / TOPIC_BATCH_SIZE);
        generationProgressService.markBlueprintReady(
                pathId,
                totalTopics,
                totalBatches,
                blueprintTopics.get(0).getName()
        );

        List<LlmService.LlmTopic> generatedTopics = new ArrayList<>();
        for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
            int fromIndex = batchIndex * TOPIC_BATCH_SIZE;
            int toIndex = Math.min(fromIndex + TOPIC_BATCH_SIZE, totalTopics);
            List<LlmService.LlmBlueprintTopic> batchTopics = blueprintTopics.subList(fromIndex, toIndex);
            String currentTopicName = batchTopics.get(0).getName();

            generationProgressService.markBatch(
                    pathId,
                    batchIndex + 1,
                    totalBatches,
                    generatedTopics.size(),
                    totalTopics,
                    currentTopicName
            );

            LlmService.LlmLearningPath batchResult = generateTopicBatchWithFallback(
                    promptProfession,
                    effectiveLevel,
                    promptContexts,
                    promptGoals,
                    blankToNull(blueprint.getPersonaSummary()) != null ? blueprint.getPersonaSummary() : personaSummary,
                    batchTopics,
                    0
            );

            if (batchResult.getTopics() == null || batchResult.getTopics().isEmpty()) {
                throw new RuntimeException("Detail generation returned no topics for batch " + (batchIndex + 1));
            }

            generatedTopics.addAll(batchResult.getTopics());

            String nextTopicName = toIndex < totalTopics ? blueprintTopics.get(toIndex).getName() : null;
            generationProgressService.markBatch(
                    pathId,
                    batchIndex + 1,
                    totalBatches,
                    Math.min(generatedTopics.size(), totalTopics),
                    totalTopics,
                    nextTopicName
            );
        }

        generationProgressService.markFinalizing(pathId, "Saving learning path and scheduling images.");
        archiveExistingActivePaths(user, pathId);

        path.setTitle(resolvePathLabel(profession, learnerProfile) + " Communication Path");
        path.setStatus(PathStatus.ACTIVE);
        replaceTopics(path, buildTopics(path, generatedTopics));

        user.setCurrentLevel(effectiveLevel);
        user.setPreferredLearningMode(LearningMode.PERSONALIZED);
        userRepository.save(user);
        LearningPath saved = learningPathRepository.save(path);

        UUID savedPathId = saved.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                imagePopulationService.populateImagesAsync(savedPathId);
            }
        });
    }

    private LlmService.LlmLearningPath generateTopicBatchWithFallback(
            String promptProfession,
            Level effectiveLevel,
            List<String> promptContexts,
            String promptGoals,
            String personaSummary,
            List<LlmService.LlmBlueprintTopic> batchTopics,
            int depth
    ) {
        try {
            return llmService.generateTopicBatch(
                    promptProfession,
                    effectiveLevel.name(),
                    promptContexts,
                    promptGoals,
                    personaSummary,
                    batchTopics
            );
        } catch (RuntimeException e) {
            if (batchTopics.size() <= 1) {
                throw e;
            }

            int midpoint = batchTopics.size() / 2;
            List<LlmService.LlmBlueprintTopic> leftTopics = new ArrayList<>(batchTopics.subList(0, midpoint));
            List<LlmService.LlmBlueprintTopic> rightTopics = new ArrayList<>(batchTopics.subList(midpoint, batchTopics.size()));

            log.warn(
                    "Topic batch failed for {} topics at depth {}. Splitting into {} and {} topics. Cause: {}",
                    batchTopics.size(),
                    depth,
                    leftTopics.size(),
                    rightTopics.size(),
                    e.getMessage()
            );

            LlmService.LlmLearningPath leftResult = generateTopicBatchWithFallback(
                    promptProfession,
                    effectiveLevel,
                    promptContexts,
                    promptGoals,
                    personaSummary,
                    leftTopics,
                    depth + 1
            );

            LlmService.LlmLearningPath rightResult = generateTopicBatchWithFallback(
                    promptProfession,
                    effectiveLevel,
                    promptContexts,
                    promptGoals,
                    personaSummary,
                    rightTopics,
                    depth + 1
            );

            LlmService.LlmLearningPath merged = new LlmService.LlmLearningPath();
            List<LlmService.LlmTopic> mergedTopics = new ArrayList<>();
            if (leftResult.getTopics() != null) {
                mergedTopics.addAll(leftResult.getTopics());
            }
            if (rightResult.getTopics() != null) {
                mergedTopics.addAll(rightResult.getTopics());
            }
            merged.setTopics(mergedTopics);
            return merged;
        }
    }

    public void markGenerationFailed(UUID pathId, String errorMessage) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status ->
            learningPathRepository.findById(pathId).ifPresent(path -> {
                path.setStatus(PathStatus.FAILED);
                learningPathRepository.save(path);
            })
        );
    }

    @Transactional(readOnly = true)
    public GenerationProgressResponse getGenerationProgress(String email, UUID pathId) {
        LearningPath path = loadOwnedPath(email, pathId);
        GenerationProgressResponse current = generationProgressService.get(pathId);
        if (current != null) {
            return current;
        }

        if (path.getStatus() == PathStatus.ACTIVE) {
            return GenerationProgressResponse.builder()
                    .pathId(pathId)
                    .phase("COMPLETE")
                    .statusText("Personalized path is ready.")
                    .progressPercent(100)
                    .complete(true)
                    .failed(false)
                    .currentBatch(0)
                    .totalBatches(0)
                    .completedTopics(path.getTopics() != null ? path.getTopics().size() : 0)
                    .totalTopics(path.getTopics() != null ? path.getTopics().size() : 0)
                    .build();
        }

        if (path.getStatus() == PathStatus.FAILED) {
            return GenerationProgressResponse.builder()
                    .pathId(pathId)
                    .phase("FAILED")
                    .statusText("Generation failed.")
                    .progressPercent(100)
                    .complete(false)
                    .failed(true)
                    .errorMessage("Generation failed before completion.")
                    .currentBatch(0)
                    .totalBatches(0)
                    .completedTopics(0)
                    .totalTopics(0)
                    .build();
        }

        return GenerationProgressResponse.builder()
                .pathId(pathId)
                .phase("QUEUED")
                .statusText("Generation is still queued.")
                .progressPercent(5)
                .complete(false)
                .failed(false)
                .currentBatch(0)
                .totalBatches(0)
                .completedTopics(0)
                .totalTopics(0)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<UUID> getLatestGeneratingPathId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return learningPathRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, PathStatus.GENERATING)
                .map(LearningPath::getId);
    }

    @Transactional
    public LearningPathResponse generatePath(String email, OnboardingRequest request) {
        throw new UnsupportedOperationException("Use startGeneration for asynchronous personalized onboarding.");
    }

    public PersonaPreviewResponse previewPersona(OnboardingRequest request) {
        String role = resolvePathLabel(null, LearnerProfile.builder()
                .jobRole(resolveJobRole(request, null))
                .industry(blankToNull(request.getIndustry()))
                .seniority(blankToNull(request.getSeniority()))
                .level(request.getLevel())
                .personaSummary(blankToNull(request.getPersonaSummary()))
                .build());

        List<String> priorities = new ArrayList<>();
        appendNonBlank(priorities, request.getCommunicateWith());
        appendNonBlank(priorities, request.getCommunicationChannels());
        appendNonBlank(priorities, request.getCommunicationContexts());
        appendNonBlank(priorities, request.getPainPoints());
        appendNonBlank(priorities, request.getGoals());
        if (request.getCustomGoal() != null && !request.getCustomGoal().isBlank()) {
            priorities.add(request.getCustomGoal().trim());
        }
        if (request.getCustomContext() != null && !request.getCustomContext().isBlank()) {
            priorities.add(request.getCustomContext().trim());
        }

        LinkedHashSet<String> deduped = priorities.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        List<String> communicationPriorities = deduped.stream()
                .limit(4)
                .toList();

        String summary = buildPersonaSummary(request, role, communicationPriorities);

        return PersonaPreviewResponse.builder()
                .personaSummary(summary)
                .communicationPriorities(communicationPriorities)
                .isAiGenerated(false)
                .build();
    }

    @Transactional(readOnly = true)
    public LearningPathResponse getActivePath(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningPath path = learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE)
                .stream().findFirst()
                .orElse(null);

        if (path == null) return null;

        // Load user progress
        Map<UUID, UserSubPhraseProgress> progressMap = new HashMap<>();
        progressRepository.findByUserId(user.getId())
                .forEach(p -> progressMap.put(p.getSubPhrase().getId(), p));
        Set<UUID> completedChunkIds = new HashSet<>();
        chunkProgressRepository.findByUserId(user.getId()).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCompleted()))
                .forEach(p -> completedChunkIds.add(p.getChunk().getId()));

        return mapToResponse(path, progressMap, completedChunkIds);
    }

    public boolean hasActivePath(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return !learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE).isEmpty();
    }

    // --- Mappers ---

    private LearningPathResponse mapToResponse(
            LearningPath path,
            Map<UUID, UserSubPhraseProgress> progressMap,
            Set<UUID> completedChunkIds
    ) {
        List<TopicResponse> topicResponses = path.getTopics().stream()
                .map(t -> mapTopicResponse(t, progressMap, completedChunkIds))
                .toList();

        int situationCount = topicResponses.stream().mapToInt(TopicResponse::getSituationCount).sum();
        int chunkCount = topicResponses.stream()
                .flatMap(t -> t.getSituations().stream())
                .mapToInt(SituationResponse::getChunkCount)
                .sum();

        return LearningPathResponse.builder()
                .id(path.getId())
                .title(path.getTitle())
                .status(path.getStatus().name())
                .learningMode(LearningMode.PERSONALIZED)
                .professionName(resolvePathLabel(path.getProfession(), path.getLearnerProfile()))
                .userLevel(resolvePathLevel(path))
                .topicCount(topicResponses.size())
                .situationCount(situationCount)
                .chunkCount(chunkCount)
                .topics(topicResponses)
                .build();
    }

    private TopicResponse mapTopicResponse(
            Topic topic,
            Map<UUID, UserSubPhraseProgress> progressMap,
            Set<UUID> completedChunkIds
    ) {
        List<SituationResponse> situations = topic.getSituations().stream()
                .map(s -> mapSituationResponse(s, progressMap, completedChunkIds))
                .toList();

        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .orderIndex(topic.getOrderIndex())
                .situationCount(situations.size())
                .situations(situations)
                .build();
    }

    private SituationResponse mapSituationResponse(
            Situation situation,
            Map<UUID, UserSubPhraseProgress> progressMap,
            Set<UUID> completedChunkIds
    ) {
        List<ChunkResponse> chunks = situation.getChunks().stream()
                .map(c -> mapChunkResponse(c, progressMap, completedChunkIds))
                .toList();

        return SituationResponse.builder()
                .id(situation.getId())
                .title(situation.getTitle())
                .description(situation.getDescription())
                .thumbnailUrl(situation.getThumbnailUrl())
                .level(situation.getLevel())
                .orderIndex(situation.getOrderIndex())
                .chunkCount(chunks.size())
                .chunks(chunks)
                .build();
    }

    private ChunkResponse mapChunkResponse(
            Chunk chunk,
            Map<UUID, UserSubPhraseProgress> progressMap,
            Set<UUID> completedChunkIds
    ) {
        List<SubPhraseResponse> subPhrases = chunk.getSubPhrases().stream()
                .map(sp -> {
                    boolean isLearned = false;
                    boolean isBookmarked = false;
                    if (progressMap != null && progressMap.containsKey(sp.getId())) {
                        UserSubPhraseProgress progress = progressMap.get(sp.getId());
                        isLearned = Boolean.TRUE.equals(progress.getIsLearned());
                        isBookmarked = Boolean.TRUE.equals(progress.getIsBookmarked());
                    }
                    List<String> distractorsList = null;
                    try {
                        if (sp.getDistractors() != null) {
                            distractorsList = objectMapper.readValue(sp.getDistractors(),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                        }
                    } catch (JsonProcessingException e) {
                        log.warn("Failed to parse distractors", e);
                    }
                    return SubPhraseResponse.builder()
                            .id(sp.getId())
                            .text(sp.getText())
                            .translation(sp.getTranslation())
                            .ipa(sp.getIpa())
                            .distractors(distractorsList)
                            .imageUrl(sp.getImageUrl())
                            .orderIndex(sp.getOrderIndex())
                            .isLearned(isLearned)
                            .isBookmarked(isBookmarked)
                            .build();
                })
                .toList();

        return ChunkResponse.builder()
                .id(chunk.getId())
                .contextQuestion(chunk.getContextQuestion())
                .contextTranslation(chunk.getContextTranslation())
                .rootSentence(chunk.getRootSentence())
                .rootTranslation(chunk.getRootTranslation())
                .rootIpa(chunk.getRootIpa())
                .orderIndex(chunk.getOrderIndex())
                .isCompleted(completedChunkIds != null && completedChunkIds.contains(chunk.getId()))
                .subPhrases(subPhrases)
                .build();
    }

    private Level parseLevel(String level) {
        try {
            return Level.valueOf(level.toUpperCase());
        } catch (Exception e) {
            return Level.BEGINNER;
        }
    }

    private LearnerProfile createLearnerProfile(OnboardingRequest request, Profession profession, Level effectiveLevel) {
        try {
            LearnerProfile profile = LearnerProfile.builder()
                    .rawPayload(objectMapper.writeValueAsString(request))
                    .jobRole(resolveJobRole(request, profession))
                    .industry(blankToNull(request.getIndustry()))
                    .seniority(blankToNull(request.getSeniority()))
                    .level(effectiveLevel)
                    .personaSummary(blankToNull(request.getPersonaSummary()))
                    .build();
            return learnerProfileRepository.save(profile);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize learner profile payload", e);
        }
    }

    private String resolveJobRole(OnboardingRequest request, Profession profession) {
        String jobRole = blankToNull(request.getJobRole());
        if (jobRole != null) {
            return jobRole;
        }
        if (profession != null) {
            return profession.getName();
        }
        return "Professional Learner";
    }

    private Profession resolveProfessionFromProfile(OnboardingRequest request) {
        String jobRole = blankToNull(request.getJobRole());
        String industry = blankToNull(request.getIndustry());
        String searchText = ((jobRole != null ? jobRole : "") + " " + (industry != null ? industry : "")).toLowerCase(Locale.ROOT);

        if (containsAny(searchText, "software engineer", "engineer", "developer", "qa", "tester", "kỹ sư", "lap trinh", "lập trình")) {
            return professionRepository.findByNameIgnoreCase("Software Engineer")
                    .orElseGet(this::fallbackProfession);
        }
        if (containsAny(searchText, "marketing", "content", "campaign", "thị trường")) {
            return professionRepository.findByNameIgnoreCase("Marketing")
                    .orElseGet(this::fallbackProfession);
        }
        if (containsAny(searchText, "finance", "accounting", "kế toán", "tài chính", "fintech")) {
            return professionRepository.findByNameIgnoreCase("Finance & Accounting")
                    .orElseGet(this::fallbackProfession);
        }
        if (containsAny(searchText, "human resources", "hr", "recruit", "nhân sự", "tuyển dụng")) {
            return professionRepository.findByNameIgnoreCase("Human Resources")
                    .orElseGet(this::fallbackProfession);
        }
        if (containsAny(searchText, "sales", "business development", "kinh doanh", "bán hàng")) {
            return professionRepository.findByNameIgnoreCase("Sales")
                    .orElseGet(this::fallbackProfession);
        }
        if (containsAny(searchText, "customer success", "customer service", "support", "dịch vụ khách hàng", "chăm sóc khách hàng")) {
            return professionRepository.findByNameIgnoreCase("Customer Service")
                    .orElseGet(this::fallbackProfession);
        }
        if (containsAny(searchText, "healthcare", "medical", "doctor", "nurse", "y tế", "bệnh viện")) {
            return professionRepository.findByNameIgnoreCase("Healthcare")
                    .orElseGet(this::fallbackProfession);
        }
        if (containsAny(searchText, "education", "teacher", "student", "giáo dục", "sinh viên", "giảng viên")) {
            return professionRepository.findByNameIgnoreCase("Education")
                    .orElseGet(this::fallbackProfession);
        }
        if (containsAny(searchText, "design", "designer", "ux", "ui", "thiết kế")) {
            return professionRepository.findByNameIgnoreCase("Design")
                    .orElseGet(this::fallbackProfession);
        }
        if (containsAny(searchText, "hospitality", "restaurant", "hotel", "f&b", "khách sạn", "nhà hàng", "dịch vụ")) {
            return professionRepository.findByNameIgnoreCase("F&B / Hospitality")
                    .orElseGet(this::fallbackProfession);
        }

        return fallbackProfession();
    }

    private Profession fallbackProfession() {
        return professionRepository.findByNameIgnoreCase("Software Engineer")
                .orElseGet(() -> professionRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No professions available")));
    }

    private boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String buildPromptProfession(Profession profession, LearnerProfile learnerProfile, OnboardingRequest request) {
        List<String> parts = new ArrayList<>();
        parts.add(resolvePathLabel(profession, learnerProfile));
        if (learnerProfile != null && learnerProfile.getIndustry() != null) {
            parts.add("Industry: " + learnerProfile.getIndustry());
        }
        if (learnerProfile != null && learnerProfile.getSeniority() != null) {
            parts.add("Seniority: " + learnerProfile.getSeniority());
        }
        if (request.getCommunicateWith() != null && !request.getCommunicateWith().isEmpty()) {
            parts.add("Communicates with: " + String.join(", ", request.getCommunicateWith()));
        }
        if (request.getCommunicationChannels() != null && !request.getCommunicationChannels().isEmpty()) {
            parts.add("Primary channels: " + String.join(", ", request.getCommunicationChannels()));
        }
        return String.join(" | ", parts);
    }

    private List<String> buildPromptContexts(OnboardingRequest request) {
        LinkedHashSet<String> contexts = new LinkedHashSet<>();
        appendNonBlank(contexts, request.getCommunicationContexts());
        appendNonBlank(contexts, request.getCommunicateWith());
        appendNonBlank(contexts, request.getCommunicationChannels());
        appendNonBlank(contexts, request.getPainPoints());
        if (request.getCustomContext() != null && !request.getCustomContext().isBlank()) {
            contexts.add(request.getCustomContext().trim());
        }
        return new ArrayList<>(contexts);
    }

    private String buildPromptGoals(OnboardingRequest request) {
        List<String> goals = new ArrayList<>();
        appendNonBlank(goals, request.getGoals());
        appendNonBlank(goals, request.getPainPoints());
        if (request.getSpecificGoals() != null && !request.getSpecificGoals().isBlank()) {
            goals.add(request.getSpecificGoals().trim());
        }
        if (request.getCustomGoal() != null && !request.getCustomGoal().isBlank()) {
            goals.add(request.getCustomGoal().trim());
        }
        return goals.isEmpty() ? null : String.join("; ", goals);
    }

    private List<String> collectCommunicationPriorities(OnboardingRequest request) {
        LinkedHashSet<String> priorities = new LinkedHashSet<>();
        appendNonBlank(priorities, request.getCommunicateWith());
        appendNonBlank(priorities, request.getCommunicationChannels());
        appendNonBlank(priorities, request.getCommunicationContexts());
        appendNonBlank(priorities, request.getPainPoints());
        appendNonBlank(priorities, request.getGoals());
        if (request.getCustomGoal() != null && !request.getCustomGoal().isBlank()) {
            priorities.add(request.getCustomGoal().trim());
        }
        if (request.getCustomContext() != null && !request.getCustomContext().isBlank()) {
            priorities.add(request.getCustomContext().trim());
        }
        return priorities.stream().limit(4).toList();
    }

    private void archiveExistingActivePaths(User user, UUID excludePathId) {
        learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE)
                .stream()
                .filter(existing -> !existing.getId().equals(excludePathId))
                .forEach(existing -> {
                    existing.setStatus(PathStatus.ARCHIVED);
                    learningPathRepository.save(existing);
                });
    }

    private LearningPath loadOwnedPath(String email, UUID pathId) {
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Learning path not found"));
        if (path.getUser() == null || !path.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You do not have access to this generation job");
        }
        return path;
    }

    private List<Topic> buildTopics(LearningPath path, List<LlmService.LlmTopic> llmTopics) {
        return IntStream.range(0, llmTopics.size())
                .mapToObj(topicIndex -> {
                    LlmService.LlmTopic llmTopic = llmTopics.get(topicIndex);
                    Topic topic = Topic.builder()
                            .name(llmTopic.getName())
                            .orderIndex(topicIndex)
                            .learningPath(path)
                            .build();

                    List<Situation> situations = IntStream.range(0, llmTopic.getSituations().size())
                            .mapToObj(situationIndex -> {
                                LlmService.LlmSituation llmSituation = llmTopic.getSituations().get(situationIndex);
                                Situation situation = Situation.builder()
                                        .title(llmSituation.getTitle())
                                        .description(llmSituation.getDescription())
                                        .level(parseLevel(llmSituation.getLevel()))
                                        .imageKeyword(llmSituation.getImageKeyword())
                                        .orderIndex(situationIndex)
                                        .topic(topic)
                                        .build();

                                List<Chunk> chunks = IntStream.range(0, llmSituation.getChunks().size())
                                        .mapToObj(chunkIndex -> {
                                            LlmService.LlmChunk llmChunk = llmSituation.getChunks().get(chunkIndex);
                                            Chunk chunk = Chunk.builder()
                                                    .contextQuestion(llmChunk.getContextQuestion())
                                                    .contextTranslation(llmChunk.getContextTranslation())
                                                    .rootSentence(llmChunk.getRootSentence())
                                                    .rootTranslation(llmChunk.getRootTranslation())
                                                    .rootIpa(llmChunk.getRootIpa())
                                                    .orderIndex(chunkIndex)
                                                    .situation(situation)
                                                    .build();

                                            if (llmChunk.getVariableChunks() != null) {
                                                List<SubPhrase> subPhrases = IntStream.range(0, llmChunk.getVariableChunks().size())
                                                        .mapToObj(subPhraseIndex -> {
                                                            LlmService.LlmVariableChunk variableChunk = llmChunk.getVariableChunks().get(subPhraseIndex);
                                                            String distractorsJson = null;
                                                            try {
                                                                if (variableChunk.getDistractors() != null) {
                                                                    distractorsJson = objectMapper.writeValueAsString(variableChunk.getDistractors());
                                                                }
                                                            } catch (JsonProcessingException e) {
                                                                log.warn("Failed to serialize distractors", e);
                                                            }

                                                            return SubPhrase.builder()
                                                                    .text(variableChunk.getText())
                                                                    .translation(variableChunk.getTranslation())
                                                                    .ipa(variableChunk.getIpa())
                                                                    .distractors(distractorsJson)
                                                                    .imageKeyword(variableChunk.getImageKeyword())
                                                                    .orderIndex(subPhraseIndex)
                                                                    .chunk(chunk)
                                                                    .build();
                                                        })
                                                        .toList();
                                                chunk.setSubPhrases(new ArrayList<>(subPhrases));
                                            }

                                            return chunk;
                                        })
                                        .toList();

                                situation.setChunks(new ArrayList<>(chunks));
                                return situation;
                            })
                            .toList();

                    topic.setSituations(new ArrayList<>(situations));
                    return topic;
                })
                .toList();
    }

    private void replaceTopics(LearningPath path, List<Topic> topics) {
        if (path.getTopics() == null) {
            path.setTopics(new ArrayList<>());
        }
        path.getTopics().clear();
        path.getTopics().addAll(topics);
    }

    private String resolvePathLabel(Profession profession, LearnerProfile learnerProfile) {
        if (profession != null) {
            return profession.getName();
        }
        if (learnerProfile != null && learnerProfile.getJobRole() != null && !learnerProfile.getJobRole().isBlank()) {
            return learnerProfile.getJobRole();
        }
        return "Personalized";
    }

    private Level resolvePathLevel(LearningPath path) {
        if (path.getLearnerProfile() != null && path.getLearnerProfile().getLevel() != null) {
            return path.getLearnerProfile().getLevel();
        }
        return path.getUser().getCurrentLevel();
    }

    private String buildPersonaSummary(OnboardingRequest request, String role, List<String> communicationPriorities) {
        String level = request.getLevel() != null ? request.getLevel().name() : "BEGINNER";
        StringBuilder summary = new StringBuilder();
        summary.append("You are building a ").append(level.toLowerCase()).append(" English path for ").append(role).append(".");

        if (request.getIndustry() != null && !request.getIndustry().isBlank()) {
            summary.append(" The learning journey is grounded in ").append(request.getIndustry().trim()).append(" scenarios.");
        }
        if (request.getSeniority() != null && !request.getSeniority().isBlank()) {
            summary.append(" Content should match ").append(request.getSeniority().trim()).append(" responsibilities.");
        }
        if (!communicationPriorities.isEmpty()) {
            summary.append(" The strongest focus areas are ").append(String.join(", ", communicationPriorities)).append(".");
        }

        return summary.toString();
    }

    private void appendNonBlank(Collection<String> target, Collection<String> values) {
        if (values == null) {
            return;
        }
        values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .forEach(target::add);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
