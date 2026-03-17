package com.fluenz.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluenz.api.dto.request.EvaluateTextRequest;
import com.fluenz.api.dto.request.PracticeCompleteRequest;
import com.fluenz.api.dto.response.*;
import com.fluenz.api.entity.*;
import com.fluenz.api.entity.enums.PathStatus;
import com.fluenz.api.repository.*;
import com.fluenz.api.service.SpeechEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
@Slf4j
public class PracticeController {

    private final SpeechEvaluationService evaluationService;
    private final UserRepository userRepository;
    private final LearningPathRepository learningPathRepository;
    private final PracticeSessionRepository practiceSessionRepository;
    private final UserSubPhraseProgressRepository progressRepository;
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

        // Find the situation from active learning path
        LearningPath path = learningPathRepository.findByUserAndStatus(user, PathStatus.ACTIVE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No active learning path"));

        Situation situation = path.getTopics().stream()
                .flatMap(t -> t.getSituations().stream())
                .filter(s -> s.getId().equals(situationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Situation not found"));

        // Build practice payload
        List<Map<String, Object>> chunks = situation.getChunks().stream()
                .sorted(Comparator.comparingInt(Chunk::getOrderIndex))
                .map(chunk -> {
                    Map<String, Object> chunkMap = new LinkedHashMap<>();
                    chunkMap.put("id", chunk.getId());
                    chunkMap.put("contextQuestion", chunk.getContextQuestion());
                    chunkMap.put("contextTranslation", chunk.getContextTranslation());
                    chunkMap.put("rootSentence", chunk.getRootSentence());
                    chunkMap.put("rootTranslation", chunk.getRootTranslation());
                    chunkMap.put("rootIpa", chunk.getRootIpa());

                    List<Map<String, Object>> variableChunks = chunk.getSubPhrases().stream()
                            .sorted(Comparator.comparingInt(SubPhrase::getOrderIndex))
                            .map(sp -> {
                                Map<String, Object> spMap = new LinkedHashMap<>();
                                spMap.put("id", sp.getId());
                                spMap.put("text", sp.getText());
                                spMap.put("translation", sp.getTranslation());
                                spMap.put("ipa", sp.getIpa());
                                try {
                                    if (sp.getDistractors() != null) {
                                        spMap.put("distractors", objectMapper.readValue(sp.getDistractors(), List.class));
                                    } else {
                                        spMap.put("distractors", List.of());
                                    }
                                } catch (JsonProcessingException e) {
                                    spMap.put("distractors", List.of());
                                }
                                return spMap;
                            })
                            .toList();

                    chunkMap.put("variableChunks", variableChunks);
                    return chunkMap;
                })
                .toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("situationId", situation.getId());
        payload.put("title", situation.getTitle());
        payload.put("description", situation.getDescription());
        payload.put("level", situation.getLevel().name());
        payload.put("chunks", chunks);

        return ResponseEntity.ok(payload);
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

        // Need to find the Situation entity — get from path
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
}
