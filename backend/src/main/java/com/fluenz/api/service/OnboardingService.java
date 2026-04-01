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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private static final int PUBLISH_WINDOW_TOPICS = 4;
    private static final int TOPIC_CONCURRENCY = 4;

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

            LearningPath stalePath = existingGenerating.get();
            stalePath.setStatus(PathStatus.FAILED);
            learningPathRepository.save(stalePath);
            log.warn("Recovered stale GENERATING learning path {} after backend restart or lost in-memory progress.", stalePath.getId());
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
                .generationPhase("QUEUED")
                .publishedTopicCount(0)
                .generatedTopicCount(0)
                .totalTopicCount(0)
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
            runBackgroundGeneration(pathId, request);
        } catch (Exception e) {
            log.error("Background onboarding generation failed for path {}: {}", pathId, e.getMessage(), e);
            markGenerationFailed(pathId, e.getMessage());
            generationProgressService.fail(pathId, e.getMessage());
        }
    }

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
            persistPersonaSummary(learnerProfile.getId(), blueprint.getPersonaSummary().trim());
        }

        List<LlmService.LlmBlueprintTopic> blueprintTopics = blueprint.getTopics();
        if (blueprintTopics == null || blueprintTopics.isEmpty()) {
            throw new RuntimeException("Blueprint generation returned no topics");
        }

        int totalTopics = blueprintTopics.size();
        generationProgressService.markBlueprintReady(
                pathId,
                totalTopics,
                totalTopics,
                blueprintTopics.get(0).getName()
        );
        updatePathGenerationMetadata(pathId, "DETAILS", 0, 0, totalTopics);

        AtomicInteger completedTopics = new AtomicInteger(0);
        String effectivePersonaSummary = blankToNull(blueprint.getPersonaSummary()) != null
                ? blueprint.getPersonaSummary()
                : personaSummary;

        int publishWindowSize = Math.min(PUBLISH_WINDOW_TOPICS, totalTopics);
        List<LlmService.LlmTopic> initiallyPublishedTopics = generateTopicsInWaves(
                promptProfession,
                effectiveLevel,
                promptContexts,
                promptGoals,
                effectivePersonaSummary,
                blueprintTopics.subList(0, publishWindowSize),
                0,
                totalTopics,
                pathId,
                completedTopics,
                null
        );

        generationProgressService.markFinalizing(pathId, "Publishing your first personalized lessons.");
        int publishedTopics = activatePathWithInitialTopics(pathId, effectiveLevel, initiallyPublishedTopics, totalTopics);
        generationProgressService.markPublishedText(
                pathId,
                "Your personalized learning path is ready. FluenZ is finishing the rest in the background.",
                publishedTopics,
                totalTopics
        );

        if (publishWindowSize < totalTopics) {
            imagePopulationService.populateImagesAsync(pathId, false);
            generateTopicsInWaves(
                    promptProfession,
                    effectiveLevel,
                    promptContexts,
                    promptGoals,
                    effectivePersonaSummary,
                    blueprintTopics.subList(publishWindowSize, totalTopics),
                    publishWindowSize,
                    totalTopics,
                    pathId,
                    completedTopics,
                    (waveStartIndex, waveTopics) -> appendTopicsToActivePath(pathId, waveTopics, waveStartIndex, totalTopics)
            );
        }

        int finalPublishedCount = readPublishedTopicCount(pathId);
        updatePathGenerationMetadata(pathId, "THUMBNAIL_HYDRATION", completedTopics.get(), finalPublishedCount, totalTopics);
        generationProgressService.markThumbnailHydration(
                pathId,
                "Core path is ready. FluenZ is hydrating thumbnails and final polish in the background.",
                finalPublishedCount,
                totalTopics
        );
        imagePopulationService.populateImagesAsync(pathId, true);
    }

    private List<LlmService.LlmTopic> generateTopicsInWaves(
            String promptProfession,
            Level effectiveLevel,
            List<String> promptContexts,
            String promptGoals,
            String personaSummary,
            List<LlmService.LlmBlueprintTopic> topicsToGenerate,
            int startTopicIndex,
            int totalTopics,
            UUID pathId,
            AtomicInteger completedTopics,
            BiFunction<Integer, List<LlmService.LlmTopic>, Integer> wavePublishCallback
    ) {
        List<LlmService.LlmTopic> orderedTopics = new ArrayList<>(Collections.nCopies(topicsToGenerate.size(), null));
        int publishedTopics = readPublishedTopicCount(pathId);

        for (int waveStart = 0; waveStart < topicsToGenerate.size(); waveStart += TOPIC_CONCURRENCY) {
            int waveEnd = Math.min(waveStart + TOPIC_CONCURRENCY, topicsToGenerate.size());
            List<CompletableFuture<GeneratedTopic>> futures = new ArrayList<>();

            for (int localIndex = waveStart; localIndex < waveEnd; localIndex++) {
                int absoluteIndex = startTopicIndex + localIndex;
                LlmService.LlmBlueprintTopic blueprintTopic = topicsToGenerate.get(localIndex);
                futures.add(CompletableFuture.supplyAsync(
                        () -> new GeneratedTopic(
                                absoluteIndex,
                                generateSingleTopicWithRetry(
                                        promptProfession,
                                        effectiveLevel,
                                        promptContexts,
                                        promptGoals,
                                        personaSummary,
                                        blueprintTopic
                                )
                        ),
                        onboardingGenerationExecutor
                ));
            }

            List<LlmService.LlmTopic> waveTopics = new ArrayList<>();
            String lastTopicName = null;
            for (CompletableFuture<GeneratedTopic> future : futures) {
                GeneratedTopic generatedTopic;
                try {
                    generatedTopic = future.join();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to generate topic wave", e);
                }

                orderedTopics.set(generatedTopic.topicIndex - startTopicIndex, generatedTopic.topic);
                waveTopics.add(generatedTopic.topic);
                lastTopicName = generatedTopic.topic.getName();
                completedTopics.incrementAndGet();
            }

            updatePathGenerationMetadata(pathId, "DETAILS", completedTopics.get(), null, totalTopics);
            if (wavePublishCallback != null && !waveTopics.isEmpty()) {
                publishedTopics = wavePublishCallback.apply(startTopicIndex + waveStart, waveTopics);
                generationProgressService.updatePublishedTopics(pathId, publishedTopics, totalTopics);
            }

            generationProgressService.markBatch(
                    pathId,
                    completedTopics.get(),
                    totalTopics,
                    completedTopics.get(),
                    publishedTopics,
                    totalTopics,
                    lastTopicName
            );
        }

        return orderedTopics.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private LlmService.LlmTopic generateSingleTopicWithRetry(
            String promptProfession,
            Level effectiveLevel,
            List<String> promptContexts,
            String promptGoals,
            String personaSummary,
            LlmService.LlmBlueprintTopic topic
    ) {
        try {
            return llmService.generateSingleTopic(
                    promptProfession,
                    effectiveLevel.name(),
                    promptContexts,
                    promptGoals,
                    personaSummary,
                    topic
            );
        } catch (RuntimeException e) {
            log.warn("Single topic generation failed for '{}' and will be retried once: {}", topic.getName(), e.getMessage());
            return llmService.generateSingleTopic(
                    promptProfession,
                    effectiveLevel.name(),
                    promptContexts,
                    promptGoals,
                    personaSummary,
                    topic
            );
        }
    }

    private void persistPersonaSummary(UUID learnerProfileId, String personaSummary) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status ->
                learnerProfileRepository.findById(learnerProfileId).ifPresent(profile -> {
                    profile.setPersonaSummary(personaSummary);
                    learnerProfileRepository.save(profile);
                })
        );
    }

    private int activatePathWithInitialTopics(UUID pathId, Level effectiveLevel, List<LlmService.LlmTopic> topics, int totalTopics) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> {
            LearningPath path = learningPathRepository.findWithGenerationContextById(pathId)
                    .orElseThrow(() -> new RuntimeException("Learning path not found"));
            User user = path.getUser();

            archiveExistingActivePaths(user, pathId);

            path.setTitle(resolvePathLabel(path.getProfession(), path.getLearnerProfile()) + " Communication Path");
            path.setStatus(PathStatus.ACTIVE);
            replaceTopics(path, buildTopics(path, topics, 0));
            path.setGenerationPhase("PUBLISHED_TEXT");
            path.setPublishedTopicCount(topics.size());
            path.setGeneratedTopicCount(Math.max(safeInt(path.getGeneratedTopicCount()), topics.size()));
            path.setTotalTopicCount(Math.max(totalTopics, topics.size()));

            user.setCurrentLevel(effectiveLevel);
            user.setPreferredLearningMode(LearningMode.PERSONALIZED);
            userRepository.save(user);
            learningPathRepository.save(path);
            return path.getPublishedTopicCount();
        });
    }

    private int appendTopicsToActivePath(UUID pathId, List<LlmService.LlmTopic> topics, int startTopicIndex, int totalTopics) {
        if (topics == null || topics.isEmpty()) {
            return readPublishedTopicCount(pathId);
        }

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> {
            LearningPath path = learningPathRepository.findById(pathId)
                    .orElseThrow(() -> new RuntimeException("Learning path not found"));

            if (path.getTopics() == null) {
                path.setTopics(new ArrayList<>());
            }
            path.getTopics().addAll(buildTopics(path, topics, startTopicIndex));
            path.setGenerationPhase("PUBLISHED_TEXT");
            path.setPublishedTopicCount(path.getTopics().size());
            path.setGeneratedTopicCount(Math.max(safeInt(path.getGeneratedTopicCount()), path.getTopics().size()));
            path.setTotalTopicCount(Math.max(totalTopics, path.getTopics().size()));
            learningPathRepository.save(path);
            return path.getPublishedTopicCount();
        });
    }

    public void markGenerationFailed(UUID pathId, String errorMessage) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status ->
            learningPathRepository.findById(pathId).ifPresent(path -> {
                path.setStatus(PathStatus.FAILED);
                path.setGenerationPhase("FAILED");
                learningPathRepository.save(path);
            })
        );
    }

    @Transactional(readOnly = true)
    public GenerationProgressResponse getGenerationProgress(String email, UUID pathId) {
        LearningPath path = loadOwnedPath(email, pathId);
        GenerationProgressResponse current = generationProgressService.get(pathId);
        if (current != null) {
            return mergeWithDurablePathState(path, current);
        }

        if (path.getStatus() == PathStatus.ACTIVE) {
            return buildDurableProgressFromPath(path);
        }

        if (path.getStatus() == PathStatus.FAILED) {
            return GenerationProgressResponse.builder()
                    .pathId(pathId)
                    .phase("FAILED")
                    .statusText("Generation failed.")
                    .progressPercent(100)
                    .textReady(false)
                    .assetsPending(false)
                    .complete(false)
                    .failed(true)
                    .errorMessage("Generation failed before completion.")
                    .currentBatch(0)
                    .totalBatches(0)
                    .completedTopics(0)
                    .publishedTopics(0)
                    .totalTopics(0)
                    .build();
        }

        return GenerationProgressResponse.builder()
                .pathId(pathId)
                .phase("QUEUED")
                .statusText("Generation is still queued.")
                .progressPercent(5)
                .textReady(false)
                .assetsPending(true)
                .complete(false)
                .failed(false)
                .currentBatch(0)
                .totalBatches(0)
                .completedTopics(0)
                .publishedTopics(0)
                .totalTopics(0)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<UUID> getLatestGeneratingPathId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<LearningPath> activePath = learningPathRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, PathStatus.ACTIVE);
        if (activePath.isPresent() && isGenerationInProgress(activePath.get())) {
            return Optional.of(activePath.get().getId());
        }

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

        LearningPath path = recoverPublishedGeneratingPathIfNeeded(user)
                .or(() -> learningPathRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, PathStatus.ACTIVE))
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
        return recoverPublishedGeneratingPathIfNeeded(user).isPresent()
                || !learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE).isEmpty();
    }

    // --- Mappers ---

    private LearningPathResponse mapToResponse(
            LearningPath path,
            Map<UUID, UserSubPhraseProgress> progressMap,
            Set<UUID> completedChunkIds
    ) {
        List<TopicResponse> topicResponses = path.getTopics().stream()
                .sorted(Comparator.comparingInt(Topic::getOrderIndex))
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
                .publishedTopicCount(Math.max(safeInt(path.getPublishedTopicCount()), topicResponses.size()))
                .generatedTopicCount(Math.max(safeInt(path.getGeneratedTopicCount()), topicResponses.size()))
                .totalTopicCount(Math.max(safeInt(path.getTotalTopicCount()), topicResponses.size()))
                .generationPhase(blankToNull(path.getGenerationPhase()) != null ? path.getGenerationPhase() : "COMPLETE")
                .generationInProgress(isGenerationInProgress(path))
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
                .sorted(Comparator.comparingInt(Situation::getOrderIndex))
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
                .sorted(Comparator.comparingInt(Chunk::getOrderIndex))
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
                .sorted(Comparator.comparingInt(SubPhrase::getOrderIndex))
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

    private Optional<LearningPath> recoverPublishedGeneratingPathIfNeeded(User user) {
        Optional<LearningPath> active = learningPathRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, PathStatus.ACTIVE);
        if (active.isPresent()) {
            return active;
        }

        Optional<LearningPath> generating = learningPathRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, PathStatus.GENERATING);
        if (generating.isEmpty()) {
            return Optional.empty();
        }

        LearningPath path = generating.get();
        int actualTopicCount = path.getTopics() == null ? 0 : path.getTopics().size();
        if (actualTopicCount == 0) {
            return Optional.empty();
        }

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        LearningPath recovered = transactionTemplate.execute(status -> {
            LearningPath managed = learningPathRepository.findById(path.getId())
                    .orElseThrow(() -> new RuntimeException("Learning path not found"));
            archiveExistingActivePaths(user, managed.getId());
            managed.setStatus(PathStatus.ACTIVE);
            managed.setGenerationPhase(coalesce(blankToNull(managed.getGenerationPhase()), "PUBLISHED_TEXT"));
            managed.setPublishedTopicCount(Math.max(safeInt(managed.getPublishedTopicCount()), managed.getTopics() == null ? 0 : managed.getTopics().size()));
            managed.setGeneratedTopicCount(Math.max(safeInt(managed.getGeneratedTopicCount()), managed.getPublishedTopicCount()));
            managed.setTotalTopicCount(Math.max(safeInt(managed.getTotalTopicCount()), managed.getGeneratedTopicCount()));
            return learningPathRepository.save(managed);
        });

        log.warn("Recovered personalized path {} from GENERATING to ACTIVE because published topics already existed.", path.getId());
        return Optional.ofNullable(recovered);
    }

    private void updatePathGenerationMetadata(
            UUID pathId,
            String phase,
            Integer generatedTopicCount,
            Integer publishedTopicCount,
            Integer totalTopicCount
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status ->
                learningPathRepository.findById(pathId).ifPresent(path -> {
                    if (phase != null) {
                        path.setGenerationPhase(phase);
                    }
                    if (generatedTopicCount != null) {
                        path.setGeneratedTopicCount(Math.max(safeInt(path.getGeneratedTopicCount()), generatedTopicCount));
                    }
                    if (publishedTopicCount != null) {
                        path.setPublishedTopicCount(Math.max(safeInt(path.getPublishedTopicCount()), publishedTopicCount));
                    }
                    if (totalTopicCount != null) {
                        path.setTotalTopicCount(Math.max(safeInt(path.getTotalTopicCount()), totalTopicCount));
                    }
                    learningPathRepository.save(path);
                })
        );
    }

    private int readPublishedTopicCount(UUID pathId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        Integer publishedCount = transactionTemplate.execute(status ->
                learningPathRepository.findById(pathId)
                        .map(path -> Math.max(safeInt(path.getPublishedTopicCount()), path.getTopics() == null ? 0 : path.getTopics().size()))
                        .orElse(0)
        );
        return publishedCount == null ? 0 : publishedCount;
    }

    private GenerationProgressResponse mergeWithDurablePathState(LearningPath path, GenerationProgressResponse current) {
        int publishedTopics = Math.max(current.getPublishedTopics(), Math.max(safeInt(path.getPublishedTopicCount()), path.getTopics() == null ? 0 : path.getTopics().size()));
        int generatedTopics = Math.max(current.getCompletedTopics(), Math.max(safeInt(path.getGeneratedTopicCount()), publishedTopics));
        int totalTopics = Math.max(current.getTotalTopics(), Math.max(safeInt(path.getTotalTopicCount()), generatedTopics));
        boolean generationInProgress = isGenerationInProgress(path);

        return current.toBuilder()
                .phase(generationInProgress ? coalesce(blankToNull(path.getGenerationPhase()), current.getPhase()) : "COMPLETE")
                .completedTopics(generatedTopics)
                .publishedTopics(publishedTopics)
                .totalTopics(totalTopics)
                .textReady(current.isTextReady() || publishedTopics > 0 || path.getStatus() == PathStatus.ACTIVE)
                .assetsPending(generationInProgress)
                .complete(!generationInProgress && path.getStatus() == PathStatus.ACTIVE)
                .failed(path.getStatus() == PathStatus.FAILED || current.isFailed())
                .build();
    }

    private GenerationProgressResponse buildDurableProgressFromPath(LearningPath path) {
        int publishedTopics = Math.max(safeInt(path.getPublishedTopicCount()), path.getTopics() == null ? 0 : path.getTopics().size());
        int generatedTopics = Math.max(safeInt(path.getGeneratedTopicCount()), publishedTopics);
        int totalTopics = Math.max(safeInt(path.getTotalTopicCount()), generatedTopics);
        boolean generationInProgress = isGenerationInProgress(path);
        String phase = generationInProgress
                ? coalesce(blankToNull(path.getGenerationPhase()), publishedTopics > 0 ? "PUBLISHED_TEXT" : "DETAILS")
                : "COMPLETE";

        return GenerationProgressResponse.builder()
                .pathId(path.getId())
                .phase(phase)
                .statusText(generationInProgress ? "FluenZ is still publishing the rest of your personalized path." : "Personalized path is ready.")
                .progressPercent(generationInProgress && totalTopics > 0
                        ? Math.min(97, Math.max(35, 35 + (int) Math.round((publishedTopics * 62.0) / totalTopics)))
                        : 100)
                .textReady(publishedTopics > 0)
                .assetsPending(generationInProgress)
                .complete(!generationInProgress)
                .failed(false)
                .currentBatch(generatedTopics)
                .totalBatches(totalTopics)
                .completedTopics(generatedTopics)
                .publishedTopics(publishedTopics)
                .totalTopics(totalTopics)
                .build();
    }

    private boolean isGenerationInProgress(LearningPath path) {
        if (path.getStatus() == PathStatus.GENERATING) {
            return true;
        }
        if (path.getStatus() != PathStatus.ACTIVE) {
            return false;
        }

        String phase = blankToNull(path.getGenerationPhase());
        int publishedTopics = Math.max(safeInt(path.getPublishedTopicCount()), path.getTopics() == null ? 0 : path.getTopics().size());
        int totalTopics = Math.max(safeInt(path.getTotalTopicCount()), publishedTopics);

        if ("COMPLETE".equalsIgnoreCase(phase)) {
            return false;
        }

        if ("THUMBNAIL_HYDRATION".equalsIgnoreCase(phase)) {
            return true;
        }

        return totalTopics > publishedTopics;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String coalesce(String first, String fallback) {
        return first != null ? first : fallback;
    }

    private List<Topic> buildTopics(LearningPath path, List<LlmService.LlmTopic> llmTopics, int startTopicIndex) {
        return IntStream.range(0, llmTopics.size())
                .mapToObj(topicIndex -> {
                    LlmService.LlmTopic llmTopic = llmTopics.get(topicIndex);
                    Topic topic = Topic.builder()
                            .name(llmTopic.getName())
                            .orderIndex(startTopicIndex + topicIndex)
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
                                        .generationStatus("PENDING_THUMBNAIL")
                                        .contentValidationStatus("VALIDATED")
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

    private record GeneratedTopic(int topicIndex, LlmService.LlmTopic topic) {}

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
