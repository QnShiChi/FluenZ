package com.fluenz.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluenz.api.dto.request.CreateDefaultChunkRequest;
import com.fluenz.api.dto.request.CreateDefaultSituationRequest;
import com.fluenz.api.dto.request.CreateDefaultSubPhraseRequest;
import com.fluenz.api.dto.request.CreateDefaultTopicRequest;
import com.fluenz.api.dto.request.UpdateDefaultChunkRequest;
import com.fluenz.api.dto.request.UpdateDefaultSituationRequest;
import com.fluenz.api.dto.request.UpdateDefaultSubPhraseRequest;
import com.fluenz.api.dto.request.UpdateDefaultTopicRequest;
import com.fluenz.api.dto.response.AdminCatalogVersionResponse;
import com.fluenz.api.dto.response.LearningPathResponse;
import com.fluenz.api.entity.DefaultChunk;
import com.fluenz.api.entity.DefaultSituation;
import com.fluenz.api.entity.DefaultCatalogVersion;
import com.fluenz.api.entity.DefaultSubPhrase;
import com.fluenz.api.entity.DefaultTopic;
import com.fluenz.api.entity.User;
import com.fluenz.api.entity.enums.DefaultCatalogVersionStatus;
import com.fluenz.api.entity.enums.LearningMode;
import com.fluenz.api.repository.DefaultCatalogVersionRepository;
import com.fluenz.api.repository.DefaultChunkRepository;
import com.fluenz.api.repository.DefaultSituationRepository;
import com.fluenz.api.repository.DefaultSubPhraseRepository;
import com.fluenz.api.repository.DefaultTopicRepository;
import com.fluenz.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultCatalogAdminService {

    private final DefaultCatalogVersionRepository defaultCatalogVersionRepository;
    private final DefaultTopicRepository defaultTopicRepository;
    private final DefaultSituationRepository defaultSituationRepository;
    private final DefaultChunkRepository defaultChunkRepository;
    private final DefaultSubPhraseRepository defaultSubPhraseRepository;
    private final UserRepository userRepository;
    private final LearningExperienceService learningExperienceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<AdminCatalogVersionResponse> listVersions() {
        return defaultCatalogVersionRepository.findAllByOrderByVersionNumberDesc().stream()
                .map(version -> AdminCatalogVersionResponse.builder()
                        .id(version.getId())
                        .versionNumber(version.getVersionNumber())
                        .title(version.getTitle())
                        .status(version.getStatus())
                        .published(Boolean.TRUE.equals(version.getPublished()))
                        .topicCount(version.getTopics().size())
                        .build())
                .toList();
    }

    @Transactional
    public AdminCatalogVersionResponse createDraftVersion() {
        int nextVersion = defaultCatalogVersionRepository.findAllByOrderByVersionNumberDesc().stream()
                .findFirst()
                .map(DefaultCatalogVersion::getVersionNumber)
                .orElse(0) + 1;

        DefaultCatalogVersion draft = DefaultCatalogVersion.builder()
                .versionNumber(nextVersion)
                .title("FluenZ Default Path v" + nextVersion)
                .status(DefaultCatalogVersionStatus.DRAFT)
                .published(false)
                .build();

        DefaultCatalogVersion saved = defaultCatalogVersionRepository.save(draft);
        return AdminCatalogVersionResponse.builder()
                .id(saved.getId())
                .versionNumber(saved.getVersionNumber())
                .title(saved.getTitle())
                .status(saved.getStatus())
                .published(Boolean.TRUE.equals(saved.getPublished()))
                .topicCount(0)
                .build();
    }

    @Transactional(readOnly = true)
    public LearningPathResponse previewVersion(UUID versionId) {
        DefaultCatalogVersion version = defaultCatalogVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Catalog version not found"));

        User previewUser = User.builder()
                .currentLevel(com.fluenz.api.entity.enums.Level.BEGINNER)
                .preferredLearningMode(LearningMode.DEFAULT)
                .build();

        return learningExperienceService.getPreviewPath(previewUser, version);
    }

    @Transactional
    public AdminCatalogVersionResponse publishVersion(UUID versionId) {
        DefaultCatalogVersion version = defaultCatalogVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Catalog version not found"));

        // Publish safeguards
        if (version.getTopics().isEmpty()) {
            throw new IllegalStateException("Khong the publish version khong co topic nao. Hay them it nhat 1 topic truoc khi publish.");
        }
        long totalSituations = version.getTopics().stream()
                .mapToLong(t -> t.getSituations().size()).sum();
        if (totalSituations == 0) {
            throw new IllegalStateException("Khong the publish version khong co situation nao. Hay them it nhat 1 situation truoc khi publish.");
        }
        defaultCatalogVersionRepository.findFirstByPublishedTrueOrderByVersionNumberDesc()
                .ifPresent(current -> {
                    current.setPublished(false);
                    current.setStatus(DefaultCatalogVersionStatus.ARCHIVED);
                    defaultCatalogVersionRepository.save(current);
                });

        version.setPublished(true);
        version.setStatus(DefaultCatalogVersionStatus.PUBLISHED);
        DefaultCatalogVersion saved = defaultCatalogVersionRepository.save(version);

        assignPublishedVersionToAllUsers(saved);

        return AdminCatalogVersionResponse.builder()
                .id(saved.getId())
                .versionNumber(saved.getVersionNumber())
                .title(saved.getTitle())
                .status(saved.getStatus())
                .published(true)
                .topicCount(saved.getTopics().size())
                .build();
    }

    @Transactional
    public Map<String, Object> createTopic(UUID versionId, CreateDefaultTopicRequest request) {
        DefaultCatalogVersion version = getVersion(versionId);

        int nextOrderIndex = version.getTopics().stream()
                .map(DefaultTopic::getOrderIndex)
                .max(Integer::compareTo)
                .orElse(-1) + 1;

        DefaultTopic topic = DefaultTopic.builder()
                .name(request.getName().trim())
                .orderIndex(nextOrderIndex)
                .catalogVersion(version)
                .build();

        DefaultTopic saved = defaultTopicRepository.save(topic);
        return Map.of("id", saved.getId(), "message", "Topic created successfully");
    }

    @Transactional
    public Map<String, Object> updateTopic(UUID topicId, UpdateDefaultTopicRequest request) {
        DefaultTopic topic = defaultTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        topic.setName(request.getName().trim());
        defaultTopicRepository.save(topic);
        return Map.of("id", topic.getId(), "message", "Topic updated successfully");
    }

    @Transactional
    public void deleteTopic(UUID topicId) {
        DefaultTopic topic = defaultTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        defaultTopicRepository.delete(topic);
    }

    @Transactional
    public Map<String, Object> createSituation(UUID topicId, CreateDefaultSituationRequest request) {
        DefaultTopic topic = defaultTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        int nextOrderIndex = topic.getSituations().stream()
                .map(DefaultSituation::getOrderIndex)
                .max(Integer::compareTo)
                .orElse(-1) + 1;

        DefaultSituation situation = DefaultSituation.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .level(request.getLevel())
                .orderIndex(nextOrderIndex)
                .topic(topic)
                .build();

        DefaultSituation saved = defaultSituationRepository.save(situation);
        return Map.of("id", saved.getId(), "message", "Situation created successfully");
    }

    @Transactional
    public Map<String, Object> updateSituation(UUID situationId, UpdateDefaultSituationRequest request) {
        DefaultSituation situation = defaultSituationRepository.findById(situationId)
                .orElseThrow(() -> new RuntimeException("Situation not found"));

        situation.setTitle(request.getTitle().trim());
        situation.setDescription(request.getDescription());
        situation.setThumbnailUrl(request.getThumbnailUrl());
        situation.setLevel(request.getLevel());
        defaultSituationRepository.save(situation);
        return Map.of("id", situation.getId(), "message", "Situation updated successfully");
    }

    @Transactional
    public void deleteSituation(UUID situationId) {
        DefaultSituation situation = defaultSituationRepository.findById(situationId)
                .orElseThrow(() -> new RuntimeException("Situation not found"));
        defaultSituationRepository.delete(situation);
    }

    @Transactional
    public Map<String, Object> createChunk(UUID situationId, CreateDefaultChunkRequest request) {
        DefaultSituation situation = defaultSituationRepository.findById(situationId)
                .orElseThrow(() -> new RuntimeException("Situation not found"));

        int nextOrderIndex = situation.getChunks().stream()
                .map(DefaultChunk::getOrderIndex)
                .max(Integer::compareTo)
                .orElse(-1) + 1;

        DefaultChunk chunk = DefaultChunk.builder()
                .contextQuestion(request.getContextQuestion().trim())
                .contextTranslation(request.getContextTranslation())
                .rootSentence(request.getRootSentence().trim())
                .rootTranslation(request.getRootTranslation())
                .rootIpa(request.getRootIpa())
                .orderIndex(nextOrderIndex)
                .situation(situation)
                .build();

        DefaultChunk saved = defaultChunkRepository.save(chunk);
        return Map.of("id", saved.getId(), "message", "Chunk created successfully");
    }

    @Transactional
    public Map<String, Object> updateChunk(UUID chunkId, UpdateDefaultChunkRequest request) {
        DefaultChunk chunk = defaultChunkRepository.findById(chunkId)
                .orElseThrow(() -> new RuntimeException("Chunk not found"));

        chunk.setContextQuestion(request.getContextQuestion().trim());
        chunk.setContextTranslation(request.getContextTranslation());
        chunk.setRootSentence(request.getRootSentence().trim());
        chunk.setRootTranslation(request.getRootTranslation());
        chunk.setRootIpa(request.getRootIpa());
        defaultChunkRepository.save(chunk);
        return Map.of("id", chunk.getId(), "message", "Chunk updated successfully");
    }

    @Transactional
    public void deleteChunk(UUID chunkId) {
        DefaultChunk chunk = defaultChunkRepository.findById(chunkId)
                .orElseThrow(() -> new RuntimeException("Chunk not found"));
        defaultChunkRepository.delete(chunk);
    }

    @Transactional
    public Map<String, Object> createSubPhrase(UUID chunkId, CreateDefaultSubPhraseRequest request) {
        DefaultChunk chunk = defaultChunkRepository.findById(chunkId)
                .orElseThrow(() -> new RuntimeException("Chunk not found"));

        int nextOrderIndex = chunk.getSubPhrases().stream()
                .map(DefaultSubPhrase::getOrderIndex)
                .max(Integer::compareTo)
                .orElse(-1) + 1;

        DefaultSubPhrase subPhrase = DefaultSubPhrase.builder()
                .text(request.getText().trim())
                .translation(request.getTranslation())
                .ipa(request.getIpa())
                .distractors(toJson(request.getDistractors()))
                .imageUrl(request.getImageUrl())
                .orderIndex(nextOrderIndex)
                .chunk(chunk)
                .build();

        DefaultSubPhrase saved = defaultSubPhraseRepository.save(subPhrase);
        return Map.of("id", saved.getId(), "message", "Sub phrase created successfully");
    }

    @Transactional
    public Map<String, Object> updateSubPhrase(UUID subPhraseId, UpdateDefaultSubPhraseRequest request) {
        DefaultSubPhrase subPhrase = defaultSubPhraseRepository.findById(subPhraseId)
                .orElseThrow(() -> new RuntimeException("Sub phrase not found"));

        subPhrase.setText(request.getText().trim());
        subPhrase.setTranslation(request.getTranslation());
        subPhrase.setIpa(request.getIpa());
        subPhrase.setDistractors(toJson(request.getDistractors()));
        subPhrase.setImageUrl(request.getImageUrl());
        defaultSubPhraseRepository.save(subPhrase);
        return Map.of("id", subPhrase.getId(), "message", "Sub phrase updated successfully");
    }

    @Transactional
    public void deleteSubPhrase(UUID subPhraseId) {
        DefaultSubPhrase subPhrase = defaultSubPhraseRepository.findById(subPhraseId)
                .orElseThrow(() -> new RuntimeException("Sub phrase not found"));
        defaultSubPhraseRepository.delete(subPhrase);
    }

    private void assignPublishedVersionToAllUsers(DefaultCatalogVersion version) {
        userRepository.findAll().forEach(user -> {
            user.setActiveDefaultCatalogVersion(version);
            userRepository.save(user);
        });
    }

    private DefaultCatalogVersion getVersion(UUID versionId) {
        return defaultCatalogVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Catalog version not found"));
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize distractors", e);
        }
    }
}
