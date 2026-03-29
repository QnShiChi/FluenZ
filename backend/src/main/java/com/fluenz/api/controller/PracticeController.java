package com.fluenz.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluenz.api.dto.request.EvaluateTextRequest;
import com.fluenz.api.dto.request.PracticeCompleteRequest;
import com.fluenz.api.dto.response.*;
import com.fluenz.api.entity.*;
import com.fluenz.api.entity.enums.LearningMode;
import com.fluenz.api.entity.enums.PathStatus;
import com.fluenz.api.repository.*;
import com.fluenz.api.service.LearningExperienceService;
import com.fluenz.api.service.RoleplayService;
import com.fluenz.api.service.SpeechEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
@Slf4j
public class PracticeController {

    private final SpeechEvaluationService evaluationService;
    private final RoleplayService roleplayService;
    private final UserRepository userRepository;
    private final LearningPathRepository learningPathRepository;
    private final PracticeSessionRepository practiceSessionRepository;
    private final DefaultPracticeSessionRepository defaultPracticeSessionRepository;
    private final UserSubPhraseProgressRepository progressRepository;
    private final UserDefaultSubPhraseProgressRepository defaultProgressRepository;
    private final SituationRepository situationRepository;
    private final DefaultSituationRepository defaultSituationRepository;
    private final LearningExperienceService learningExperienceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/evaluate-text")
    public ResponseEntity<EvaluationResponse> evaluateText(
            @Valid @RequestBody EvaluateTextRequest request
    ) {
        EvaluationResponse result = evaluationService.evaluate(
                request.getExpectedText(),
                request.getActualText()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{situationId}/start")
    public ResponseEntity<Map<String, Object>> startPractice(
            @PathVariable UUID situationId,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPreferredLearningMode() == LearningMode.DEFAULT) {
            DefaultCatalogVersion version = learningExperienceService.resolveAssignedDefaultVersion(user);
            if (version == null) {
                throw new RuntimeException("No active default catalog");
            }

            DefaultSituation situation = defaultSituationRepository.findByIdAndTopicCatalogVersionId(situationId, version.getId())
                    .orElseThrow(() -> new RuntimeException("Situation not found"));
            return ResponseEntity.ok(buildDefaultPracticePayload(situation));
        }

        LearningPath path = learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No active learning path"));

        Situation situation = path.getTopics().stream()
                .flatMap(t -> t.getSituations().stream())
                .filter(s -> s.getId().equals(situationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Situation not found"));

        return ResponseEntity.ok(buildPersonalizedPracticePayload(situation));
    }

    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completePractice(
            @RequestBody PracticeCompleteRequest request,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String failedWordsJson = null;
        try {
            if (request.getFailedWords() != null && !request.getFailedWords().isEmpty()) {
                failedWordsJson = objectMapper.writeValueAsString(request.getFailedWords());
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize failed words", e);
        }

        if (user.getPreferredLearningMode() == LearningMode.DEFAULT) {
            DefaultCatalogVersion version = learningExperienceService.resolveAssignedDefaultVersion(user);
            if (version == null) {
                throw new RuntimeException("No active default catalog");
            }

            DefaultSituation situation = defaultSituationRepository.findByIdAndTopicCatalogVersionId(request.getSituationId(), version.getId())
                    .orElseThrow(() -> new RuntimeException("Situation not found"));

            DefaultPracticeSession session = DefaultPracticeSession.builder()
                    .user(user)
                    .defaultSituation(situation)
                    .totalTimeSeconds(request.getTotalTimeSeconds())
                    .overallScore(request.getOverallScore())
                    .failedWords(failedWordsJson)
                    .completedAt(LocalDateTime.now())
                    .build();

            DefaultPracticeSession saved = defaultPracticeSessionRepository.save(session);
            return ResponseEntity.ok(Map.of(
                    "sessionId", saved.getId(),
                    "message", "Default practice session saved successfully"
            ));
        }

        LearningPath path = learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No active learning path"));

        Situation situation = path.getTopics().stream()
                .flatMap(t -> t.getSituations().stream())
                .filter(s -> s.getId().equals(request.getSituationId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Situation not found"));

        PracticeSession session = PracticeSession.builder()
                .user(user)
                .situation(situation)
                .totalTimeSeconds(request.getTotalTimeSeconds())
                .overallScore(request.getOverallScore())
                .failedWords(failedWordsJson)
                .completedAt(LocalDateTime.now())
                .build();

        PracticeSession saved = practiceSessionRepository.save(session);

        return ResponseEntity.ok(Map.of(
                "sessionId", saved.getId(),
                "message", "Practice session saved successfully"
        ));
    }

    // ==================== ROLEPLAY CHAT ====================

    @PostMapping("/roleplay/chat")
    public ResponseEntity<?> roleplayChat(@RequestBody Map<String, Object> request) {
        try {
            String situationId = (String) request.get("situationId");
            List<Map<String, String>> chatHistory = (List<Map<String, String>>) request.get("chatHistory");
            int turnNumber = (int) request.get("turnNumber");

            Optional<Situation> personalizedSituation = situationRepository.findById(UUID.fromString(situationId));
            Optional<DefaultSituation> defaultSituation = defaultSituationRepository.findById(UUID.fromString(situationId));

            String title;
            String description;
            List<String> targetChunks;

            if (personalizedSituation.isPresent()) {
                Situation situation = personalizedSituation.get();
                title = situation.getTitle();
                description = situation.getDescription();
                targetChunks = situation.getChunks().stream()
                        .flatMap(chunk -> chunk.getSubPhrases().stream())
                        .map(SubPhrase::getText)
                        .toList();
            } else if (defaultSituation.isPresent()) {
                DefaultSituation situation = defaultSituation.get();
                title = situation.getTitle();
                description = situation.getDescription();
                targetChunks = situation.getChunks().stream()
                        .flatMap(chunk -> chunk.getSubPhrases().stream())
                        .map(DefaultSubPhrase::getText)
                        .toList();
            } else {
                throw new RuntimeException("Situation not found");
            }

            String response = roleplayService.chat(title, description, targetChunks, chatHistory, turnNumber);

            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            log.error("Roleplay chat error", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> buildPersonalizedPracticePayload(Situation situation) {
        List<Map<String, Object>> chunks = situation.getChunks().stream()
                .sorted(Comparator.comparingInt(Chunk::getOrderIndex))
                .map(chunk -> buildChunkMap(
                        chunk.getId(),
                        chunk.getContextQuestion(),
                        chunk.getContextTranslation(),
                        chunk.getRootSentence(),
                        chunk.getRootTranslation(),
                        chunk.getRootIpa(),
                        chunk.getSubPhrases().stream()
                                .sorted(Comparator.comparingInt(SubPhrase::getOrderIndex))
                                .map(sp -> buildVariableChunkMap(
                                        sp.getId(),
                                        sp.getText(),
                                        sp.getTranslation(),
                                        sp.getIpa(),
                                        sp.getDistractors(),
                                        sp.getImageUrl()
                                ))
                                .toList()
                ))
                .toList();

        return buildPracticePayload(situation.getId(), situation.getTitle(), situation.getDescription(), situation.getLevel().name(), chunks);
    }

    private Map<String, Object> buildDefaultPracticePayload(DefaultSituation situation) {
        List<Map<String, Object>> chunks = situation.getChunks().stream()
                .sorted(Comparator.comparingInt(DefaultChunk::getOrderIndex))
                .map(chunk -> buildChunkMap(
                        chunk.getId(),
                        chunk.getContextQuestion(),
                        chunk.getContextTranslation(),
                        chunk.getRootSentence(),
                        chunk.getRootTranslation(),
                        chunk.getRootIpa(),
                        chunk.getSubPhrases().stream()
                                .sorted(Comparator.comparingInt(DefaultSubPhrase::getOrderIndex))
                                .map(sp -> buildVariableChunkMap(
                                        sp.getId(),
                                        sp.getText(),
                                        sp.getTranslation(),
                                        sp.getIpa(),
                                        sp.getDistractors(),
                                        sp.getImageUrl()
                                ))
                                .toList()
                ))
                .toList();

        return buildPracticePayload(situation.getId(), situation.getTitle(), situation.getDescription(), situation.getLevel().name(), chunks);
    }

    private Map<String, Object> buildChunkMap(
            UUID chunkId,
            String contextQuestion,
            String contextTranslation,
            String rootSentence,
            String rootTranslation,
            String rootIpa,
            List<Map<String, Object>> variableChunks
    ) {
        Map<String, Object> chunkMap = new LinkedHashMap<>();
        chunkMap.put("id", chunkId);
        chunkMap.put("contextQuestion", contextQuestion);
        chunkMap.put("contextTranslation", contextTranslation);
        chunkMap.put("rootSentence", rootSentence);
        chunkMap.put("rootTranslation", rootTranslation);
        chunkMap.put("rootIpa", rootIpa);
        chunkMap.put("variableChunks", variableChunks);
        return chunkMap;
    }

    private Map<String, Object> buildVariableChunkMap(
            UUID id,
            String text,
            String translation,
            String ipa,
            String distractorsJson,
            String imageUrl
    ) {
        Map<String, Object> spMap = new LinkedHashMap<>();
        spMap.put("id", id);
        spMap.put("text", text);
        spMap.put("translation", translation);
        spMap.put("ipa", ipa);
        try {
            if (distractorsJson != null) {
                spMap.put("distractors", objectMapper.readValue(distractorsJson, List.class));
            } else {
                spMap.put("distractors", List.of());
            }
        } catch (JsonProcessingException e) {
            spMap.put("distractors", List.of());
        }
        spMap.put("imageUrl", imageUrl);
        return spMap;
    }

    private Map<String, Object> buildPracticePayload(UUID situationId, String title, String description, String level, List<Map<String, Object>> chunks) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("situationId", situationId);
        payload.put("title", title);
        payload.put("description", description);
        payload.put("level", level);
        payload.put("chunks", chunks);
        return payload;
    }
}
