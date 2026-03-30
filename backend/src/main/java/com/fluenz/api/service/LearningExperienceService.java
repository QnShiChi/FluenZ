package com.fluenz.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluenz.api.dto.response.*;
import com.fluenz.api.entity.*;
import com.fluenz.api.entity.enums.DefaultCatalogVersionStatus;
import com.fluenz.api.entity.enums.LearningMode;
import com.fluenz.api.entity.enums.PathStatus;
import com.fluenz.api.repository.DefaultCatalogVersionRepository;
import com.fluenz.api.repository.LearningPathRepository;
import com.fluenz.api.repository.UserDefaultSubPhraseProgressRepository;
import com.fluenz.api.repository.UserDefaultChunkProgressRepository;
import com.fluenz.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningExperienceService {

    private final UserRepository userRepository;
    private final LearningPathRepository learningPathRepository;
    private final DefaultCatalogVersionRepository defaultCatalogVersionRepository;
    private final UserDefaultSubPhraseProgressRepository userDefaultSubPhraseProgressRepository;
    private final UserDefaultChunkProgressRepository userDefaultChunkProgressRepository;
    private final OnboardingService onboardingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public LearningPathResponse getActivePath(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPreferredLearningMode() == LearningMode.DEFAULT) {
            DefaultCatalogVersion assignedVersion = resolveAssignedDefaultVersion(user);
            if (assignedVersion == null) {
                return null;
            }
            return mapDefaultCatalogToResponse(user, assignedVersion);
        }

        return onboardingService.getActivePath(email);
    }

    @Transactional(readOnly = true)
    public LearningPathResponse getPreviewPath(User user, DefaultCatalogVersion version) {
        return mapDefaultCatalogToResponse(user, version);
    }

    @Transactional
    public LearningModeResponse updateLearningMode(String email, LearningMode learningMode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean onboardingRequired = false;
        if (learningMode == LearningMode.PERSONALIZED) {
            boolean hasPersonalizedPath =
                    !learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE).isEmpty()
                    || !learningPathRepository.findByUserAndStatus(user, PathStatus.GENERATING).isEmpty();
            onboardingRequired = !hasPersonalizedPath;
        } else {
            DefaultCatalogVersion version = resolveAssignedDefaultVersion(user);
            if (version == null) {
                throw new IllegalStateException("Chua co default catalog duoc publish. Hay restart backend hoac publish mot version mac dinh.");
            }
        }

        user.setPreferredLearningMode(learningMode);
        userRepository.save(user);

        return LearningModeResponse.builder()
                .preferredLearningMode(learningMode)
                .onboardingRequired(onboardingRequired)
                .build();
    }

    @Transactional
    public DefaultCatalogVersion resolveAssignedDefaultVersion(User user) {
        if (user.getActiveDefaultCatalogVersion() != null) {
            return user.getActiveDefaultCatalogVersion();
        }

        DefaultCatalogVersion published = defaultCatalogVersionRepository
                .findFirstByPublishedTrueOrderByVersionNumberDesc()
                .or(() -> defaultCatalogVersionRepository.findFirstByStatusOrderByVersionNumberDesc(DefaultCatalogVersionStatus.PUBLISHED))
                .orElse(null);

        if (published != null) {
            user.setActiveDefaultCatalogVersion(published);
            userRepository.save(user);
        }
        return published;
    }

    private LearningPathResponse mapDefaultCatalogToResponse(User user, DefaultCatalogVersion version) {
        Map<UUID, UserDefaultSubPhraseProgress> progressMap = new HashMap<>();
        Set<UUID> completedChunkIds = new HashSet<>();
        
        if (user.getId() != null) {
            userDefaultSubPhraseProgressRepository.findByUserId(user.getId())
                    .forEach(p -> progressMap.put(p.getDefaultSubPhrase().getId(), p));
            userDefaultChunkProgressRepository.findByUserId(user.getId())
                    .stream().filter(p -> Boolean.TRUE.equals(p.getIsCompleted()))
                    .forEach(p -> completedChunkIds.add(p.getDefaultChunk().getId()));
        }

        List<TopicResponse> topicResponses = version.getTopics().stream()
                .sorted(Comparator.comparingInt(DefaultTopic::getOrderIndex))
                .map(topic -> mapDefaultTopic(topic, progressMap, completedChunkIds))
                .toList();

        int situationCount = topicResponses.stream().mapToInt(TopicResponse::getSituationCount).sum();
        int chunkCount = topicResponses.stream()
                .flatMap(t -> t.getSituations().stream())
                .mapToInt(SituationResponse::getChunkCount)
                .sum();

        return LearningPathResponse.builder()
                .id(version.getId())
                .title(version.getTitle())
                .status(version.getStatus().name())
                .learningMode(LearningMode.DEFAULT)
                .professionName("Default Catalog")
                .userLevel(user.getCurrentLevel())
                .topicCount(topicResponses.size())
                .situationCount(situationCount)
                .chunkCount(chunkCount)
                .topics(topicResponses)
                .build();
    }

    private TopicResponse mapDefaultTopic(DefaultTopic topic, Map<UUID, UserDefaultSubPhraseProgress> progressMap, Set<UUID> completedChunkIds) {
        List<SituationResponse> situations = topic.getSituations().stream()
                .sorted(Comparator.comparingInt(DefaultSituation::getOrderIndex))
                .map(situation -> mapDefaultSituation(situation, progressMap, completedChunkIds))
                .toList();

        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .orderIndex(topic.getOrderIndex())
                .situationCount(situations.size())
                .situations(situations)
                .build();
    }

    private SituationResponse mapDefaultSituation(DefaultSituation situation, Map<UUID, UserDefaultSubPhraseProgress> progressMap, Set<UUID> completedChunkIds) {
        List<ChunkResponse> chunks = situation.getChunks().stream()
                .sorted(Comparator.comparingInt(DefaultChunk::getOrderIndex))
                .map(chunk -> mapDefaultChunk(chunk, progressMap, completedChunkIds))
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

    private ChunkResponse mapDefaultChunk(DefaultChunk chunk, Map<UUID, UserDefaultSubPhraseProgress> progressMap, Set<UUID> completedChunkIds) {
        List<SubPhraseResponse> subPhrases = chunk.getSubPhrases().stream()
                .sorted(Comparator.comparingInt(DefaultSubPhrase::getOrderIndex))
                .map(sp -> {
                    boolean isLearned = false;
                    boolean isBookmarked = false;
                    if (progressMap.containsKey(sp.getId())) {
                        UserDefaultSubPhraseProgress progress = progressMap.get(sp.getId());
                        isLearned = Boolean.TRUE.equals(progress.getIsLearned());
                        isBookmarked = Boolean.TRUE.equals(progress.getIsBookmarked());
                    }

                    List<String> distractors = parseDistractors(sp.getDistractors());
                    return SubPhraseResponse.builder()
                            .id(sp.getId())
                            .text(sp.getText())
                            .translation(sp.getTranslation())
                            .ipa(sp.getIpa())
                            .distractors(distractors)
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
                .isCompleted(completedChunkIds.contains(chunk.getId()))
                .subPhrases(subPhrases)
                .build();
    }

    private List<String> parseDistractors(String distractorsJson) {
        if (distractorsJson == null || distractorsJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    distractorsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse default distractors", e);
            return List.of();
        }
    }
}
