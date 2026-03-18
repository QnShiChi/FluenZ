package com.fluenz.api.service;

import com.fluenz.api.dto.request.OnboardingRequest;
import com.fluenz.api.dto.response.*;
import com.fluenz.api.entity.*;
import com.fluenz.api.entity.enums.Level;
import com.fluenz.api.entity.enums.PathStatus;
import com.fluenz.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final UserRepository userRepository;
    private final ProfessionRepository professionRepository;
    private final LearningPathRepository learningPathRepository;
    private final UserSubPhraseProgressRepository progressRepository;
    private final LlmService llmService;
    private final ImageService imageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public LearningPathResponse generatePath(String email, OnboardingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profession profession = professionRepository.findById(request.getProfessionId())
                .orElseThrow(() -> new RuntimeException("Profession not found"));

        // Archive existing active paths
        learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE)
                .forEach(path -> {
                    path.setStatus(PathStatus.ARCHIVED);
                    learningPathRepository.save(path);
                });

        // Call LLM
        LlmService.LlmLearningPath llmResult = llmService.generateLearningPath(
                profession.getName(),
                user.getCurrentLevel().name(),
                request.getCommunicationContexts(),
                request.getSpecificGoals()
        );

        // Build and save entities
        LearningPath path = LearningPath.builder()
                .title(profession.getName() + " Communication Path")
                .status(PathStatus.ACTIVE)
                .user(user)
                .profession(profession)
                .build();

        List<Topic> topics = IntStream.range(0, llmResult.getTopics().size())
                .mapToObj(ti -> {
                    LlmService.LlmTopic lt = llmResult.getTopics().get(ti);
                    Topic topic = Topic.builder()
                            .name(lt.getName())
                            .orderIndex(ti)
                            .learningPath(path)
                            .build();

                    List<Situation> situations = IntStream.range(0, lt.getSituations().size())
                            .mapToObj(si -> {
                                LlmService.LlmSituation ls = lt.getSituations().get(si);
                                Situation situation = Situation.builder()
                                        .title(ls.getTitle())
                                        .description(ls.getDescription())
                                        .level(parseLevel(ls.getLevel()))
                                        .orderIndex(si)
                                        .topic(topic)
                                        .build();

                                List<Chunk> chunks = IntStream.range(0, ls.getChunks().size())
                                        .mapToObj(ci -> {
                                            LlmService.LlmChunk lc = ls.getChunks().get(ci);
                                            Chunk chunk = Chunk.builder()
                                                    .contextQuestion(lc.getContextQuestion())
                                                    .contextTranslation(lc.getContextTranslation())
                                                    .rootSentence(lc.getRootSentence())
                                                    .rootTranslation(lc.getRootTranslation())
                                                    .rootIpa(lc.getRootIpa())
                                                    .orderIndex(ci)
                                                    .situation(situation)
                                                    .build();

                                            if (lc.getVariableChunks() != null) {
                                                List<SubPhrase> subPhrases = IntStream.range(0, lc.getVariableChunks().size())
                                                        .mapToObj(spi -> {
                                                            LlmService.LlmVariableChunk vc = lc.getVariableChunks().get(spi);
                                                            String distractorsJson = null;
                                                            try {
                                                                if (vc.getDistractors() != null) {
                                                                    distractorsJson = objectMapper.writeValueAsString(vc.getDistractors());
                                                                }
                                                            } catch (JsonProcessingException e) {
                                                                log.warn("Failed to serialize distractors", e);
                                                            }
                                                            String imageUrl = null;
                                                            if (vc.getImageKeyword() != null && !vc.getImageKeyword().isBlank()) {
                                                                imageUrl = imageService.fetchImageUrl(vc.getImageKeyword());
                                                            }
                                                            return SubPhrase.builder()
                                                                    .text(vc.getText())
                                                                    .translation(vc.getTranslation())
                                                                    .ipa(vc.getIpa())
                                                                    .distractors(distractorsJson)
                                                                    .imageUrl(imageUrl)
                                                                    .orderIndex(spi)
                                                                    .chunk(chunk)
                                                                    .build();
                                                        })
                                                        .toList();
                                                chunk.setSubPhrases(new ArrayList<>(subPhrases));
                                            }
                                            return chunk;
                                        }).toList();

                                situation.setChunks(new ArrayList<>(chunks));
                                return situation;
                            }).toList();

                    topic.setSituations(new ArrayList<>(situations));
                    return topic;
                }).toList();

        path.setTopics(new ArrayList<>(topics));
        LearningPath saved = learningPathRepository.save(path);

        return mapToResponse(saved, null);
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

        return mapToResponse(path, progressMap);
    }

    public boolean hasActivePath(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return !learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE).isEmpty();
    }

    // --- Mappers ---

    private LearningPathResponse mapToResponse(LearningPath path, Map<UUID, UserSubPhraseProgress> progressMap) {
        List<TopicResponse> topicResponses = path.getTopics().stream()
                .map(t -> mapTopicResponse(t, progressMap))
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
                .professionName(path.getProfession().getName())
                .userLevel(path.getUser().getCurrentLevel())
                .topicCount(topicResponses.size())
                .situationCount(situationCount)
                .chunkCount(chunkCount)
                .topics(topicResponses)
                .build();
    }

    private TopicResponse mapTopicResponse(Topic topic, Map<UUID, UserSubPhraseProgress> progressMap) {
        List<SituationResponse> situations = topic.getSituations().stream()
                .map(s -> mapSituationResponse(s, progressMap))
                .toList();

        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .orderIndex(topic.getOrderIndex())
                .situationCount(situations.size())
                .situations(situations)
                .build();
    }

    private SituationResponse mapSituationResponse(Situation situation, Map<UUID, UserSubPhraseProgress> progressMap) {
        List<ChunkResponse> chunks = situation.getChunks().stream()
                .map(c -> mapChunkResponse(c, progressMap))
                .toList();

        return SituationResponse.builder()
                .id(situation.getId())
                .title(situation.getTitle())
                .description(situation.getDescription())
                .level(situation.getLevel())
                .orderIndex(situation.getOrderIndex())
                .chunkCount(chunks.size())
                .chunks(chunks)
                .build();
    }

    private ChunkResponse mapChunkResponse(Chunk chunk, Map<UUID, UserSubPhraseProgress> progressMap) {
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
}
