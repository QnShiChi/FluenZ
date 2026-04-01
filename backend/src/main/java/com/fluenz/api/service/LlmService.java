package com.fluenz.api.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class LlmService {

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    @Value("${openrouter.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .build();
    }

    // --- LLM Response DTOs ---
    @Data
    public static class LlmLearningPath {
        private List<LlmTopic> topics;
    }

    @Data
    public static class LlmBlueprint {
        private String personaSummary;
        private List<String> communicationPriorities;
        private List<LlmBlueprintTopic> topics;
    }

    @Data
    public static class LlmBlueprintTopic {
        private String name;
        private String description;
        private String level;
        private String imageKeyword;
    }

    @Data
    public static class LlmTopic {
        private String name;
        private List<LlmSituation> situations;
    }

    @Data
    public static class LlmSituation {
        private String title;
        private String description;
        private String level;
        private String imageKeyword;
        private List<LlmChunk> chunks;
    }

    @Data
    public static class LlmChunk {
        @JsonProperty("contextQuestion")
        private String contextQuestion;
        @JsonProperty("contextTranslation")
        private String contextTranslation;
        @JsonProperty("rootSentence")
        private String rootSentence;
        @JsonProperty("rootTranslation")
        private String rootTranslation;
        @JsonProperty("rootIpa")
        private String rootIpa;
        @JsonProperty("rootDistractors")
        private List<String> rootDistractors;
        @JsonProperty("variableChunks")
        private List<LlmVariableChunk> variableChunks;
    }

    @Data
    public static class LlmVariableChunk {
        private String text;
        private String translation;
        private String ipa;
        private List<String> distractors;
        private String imageKeyword;
    }

    public LlmLearningPath generateLearningPath(
            String profession,
            String level,
            List<String> contexts,
            String goals
    ) {
        String prompt = buildLegacyPrompt(profession, level, contexts, goals);
        return executeLearningPath(prompt, 8192, "Failed to generate learning path after 3 attempts");
    }

    public LlmBlueprint generateBlueprint(
            String profession,
            String level,
            List<String> contexts,
            String goals,
            String personaSummary
    ) {
        String prompt = buildBlueprintPrompt(profession, level, contexts, goals, personaSummary);

        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                String response = callOpenRouter(prompt, 8192);
                return parseBlueprintResponse(response);
            } catch (Exception e) {
                log.warn("Blueprint attempt {} failed: {}", attempt + 1, e.getMessage());
                if (attempt == 2) {
                    throw new RuntimeException("Failed to generate learning blueprint after 3 attempts", e);
                }
            }
        }

        throw new RuntimeException("Unreachable");
    }

    public LlmLearningPath generateTopicBatch(
            String profession,
            String level,
            List<String> contexts,
            String goals,
            String personaSummary,
            List<LlmBlueprintTopic> batchTopics
    ) {
        String prompt = buildTopicBatchPrompt(profession, level, contexts, goals, personaSummary, batchTopics);
        return executeLearningPath(prompt, 12288, "Failed to generate topic batch after 3 attempts");
    }

    private LlmLearningPath executeLearningPath(String prompt, int maxTokens, String failureMessage) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                String response = callOpenRouter(prompt, maxTokens);
                return parseLearningPathResponse(response);
            } catch (Exception e) {
                log.warn("Learning-path attempt {} failed: {}", attempt + 1, e.getMessage());
                if (attempt == 2) {
                    throw new RuntimeException(failureMessage, e);
                }
            }
        }
        throw new RuntimeException("Unreachable");
    }

    private String buildLegacyPrompt(String profession, String level, List<String> contexts, String goals) {
        String contextStr = contexts != null && !contexts.isEmpty()
                ? String.join(", ", contexts)
                : "general professional communication";
        String goalsStr = goals != null && !goals.isEmpty()
                ? goals
                : "improve professional English communication";

        return """
                You are an expert English language learning content designer for Vietnamese professionals.
                You use the "Pattern & Slotting" method: instead of memorizing full sentences,
                learners master a root sentence pattern (with blanks) and learn interchangeable chunks to fill in.
                
                Generate a personalized English learning path for:
                - Profession: %s
                - Current Level: %s
                - Communication Contexts: %s
                - Specific Goals: %s
                
                Create a structured learning path with:
                - 3-5 topics (thematic groups)
                - Each topic has 2-4 situations (real-world scenarios)
                - Each situation has 2-4 chunks, where each chunk follows the Pattern & Slotting structure:
                  * contextQuestion: A realistic English question that triggers the response
                  * contextTranslation: Vietnamese translation of that question
                  * rootSentence: The core response pattern with blanks marked as "___"
                  * rootTranslation: Vietnamese translation of the root sentence
                  * rootIpa: IPA phonetic transcription of rootSentence (without the blanks)
                  * rootDistractors: 2 wrong English phrases that look similar but are incorrect alternatives for the root
                  * variableChunks: EXACTLY 3 objects, each with text, ipa (IPA phonetics), and distractors (2 wrong alternatives)
                
                Rules:
                1. Topic names in Vietnamese
                2. Situation titles in Vietnamese, descriptions as context-setting in Vietnamese
                3. Situation level: BEGINNER, INTERMEDIATE, or ADVANCED
                4. Each situation.imageKeyword is a 2-4 word English phrase suitable for stock photo search that represents the situation's context (e.g., "office introduction handshake", "email writing laptop")
                5. contextQuestion must be a natural English question
                6. rootSentence must contain EXACTLY ONE "___" blank where all variable chunks slot in
                7. Each variableChunk.text is a short English phrase (2-6 words)
                8. Each variableChunk.translation is the Vietnamese meaning of variableChunk.text
                9. Each variableChunk.distractors has exactly 2 wrong but plausible alternatives
                10. rootDistractors has exactly 2 wrong but plausible alternatives for the root sentence
                11. IPA must be accurate International Phonetic Alphabet transcription
                12. Each variableChunk.imageKeyword is a 2-4 word English phrase suitable for stock photo search (e.g., "team meeting whiteboard", "user feedback laptop")
                13. CRITICAL: You MUST generate EXACTLY 3 variableChunks for each chunk. Not 2, not 4 — exactly 3.
                14. CRITICAL: All 3 variableChunks must be interchangeable alternatives for the SAME single blank in rootSentence. Do not create multiple blanks or multi-slot templates.
                
                Example chunk:
                {
                  "contextQuestion": "Why do we need to change the design now?",
                  "contextTranslation": "Tại sao chúng ta cần thay đổi thiết kế bây giờ?",
                  "rootSentence": "We need to change ___.",
                  "rootTranslation": "Chúng ta cần thay đổi ___.",
                  "rootIpa": "/wiː niːd tuː tʃeɪndʒ/",
                  "rootDistractors": ["We want to keep", "They have to build"],
                  "variableChunks": [
                    {"text": "due to new requirements", "translation": "do yêu cầu mới", "ipa": "/djuː tuː njuː rɪˈkwaɪəmənts/", "distractors": ["because of old features", "thanks to the budget"], "imageKeyword": "requirements document office"},
                    {"text": "because of user feedback", "translation": "vì phản hồi người dùng", "ipa": "/bɪˈkɒz ɒv ˈjuːzə ˈfiːdbæk/", "distractors": ["due to team decision", "for the next sprint"], "imageKeyword": "user feedback laptop"},
                    {"text": "for technical reasons", "translation": "vì lý do kỹ thuật", "ipa": "/fɔːr ˈteknɪkl ˈriːznz/", "distractors": ["with business purposes", "from design changes"], "imageKeyword": "technical engineering code"}
                  ]
                }
                
                Respond ONLY with valid JSON (no markdown, no explanation):
                {
                  "topics": [
                    {
                      "name": "Topic in Vietnamese",
                      "situations": [
                        {
                          "title": "Situation in Vietnamese",
                          "description": "Context in Vietnamese",
                          "level": "BEGINNER",
                          "imageKeyword": "situation stock photo keyword",
                          "chunks": [
                            {
                              "contextQuestion": "English question",
                              "contextTranslation": "Vietnamese translation",
                              "rootSentence": "Pattern with ___.",
                              "rootTranslation": "Vietnamese pattern",
                              "rootIpa": "/IPA/",
                              "rootDistractors": ["wrong root 1", "wrong root 2"],
                              "variableChunks": [
                                {"text": "chunk text", "translation": "nghĩa tiếng Việt", "ipa": "/IPA/", "distractors": ["wrong 1", "wrong 2"], "imageKeyword": "search keyword"}
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """.formatted(profession, level, contextStr, goalsStr);
    }

    private String buildBlueprintPrompt(String profession, String level, List<String> contexts, String goals, String personaSummary) {
        String contextStr = contexts != null && !contexts.isEmpty()
                ? String.join(", ", contexts)
                : "general professional communication";
        String goalsStr = goals != null && !goals.isEmpty()
                ? goals
                : "improve professional English communication";
        String personaSeed = personaSummary != null && !personaSummary.isBlank()
                ? personaSummary
                : "Create a grounded learner persona before generating the roadmap.";

        return """
                You are designing a deep English communication roadmap for a Vietnamese learner.
                
                Learner profile:
                - Role context: %s
                - Current level: %s
                - Communication contexts: %s
                - Goals: %s
                - Persona seed: %s
                
                Produce ONLY valid JSON with:
                - personaSummary: 3-4 sentences about the learner
                - communicationPriorities: 3-5 concise priorities
                - topics: 20 topic roadmap entries ordered from foundational to advanced
                
                Topic rules:
                1. Topic names and descriptions should be in Vietnamese.
                2. Each topic must include level: BEGINNER, INTERMEDIATE, or ADVANCED.
                3. Each topic must include imageKeyword: a 2-4 word English stock-photo phrase.
                4. Topics should progress in difficulty and stay tightly connected to the learner profile.
                
                JSON shape:
                {
                  "personaSummary": "string",
                  "communicationPriorities": ["priority 1", "priority 2"],
                  "topics": [
                    {
                      "name": "Topic in Vietnamese",
                      "description": "Why this topic matters in Vietnamese",
                      "level": "BEGINNER",
                      "imageKeyword": "meeting room laptop"
                    }
                  ]
                }
                """.formatted(profession, level, contextStr, goalsStr, personaSeed);
    }

    private String buildTopicBatchPrompt(
            String profession,
            String level,
            List<String> contexts,
            String goals,
            String personaSummary,
            List<LlmBlueprintTopic> batchTopics
    ) {
        String contextStr = contexts != null && !contexts.isEmpty()
                ? String.join(", ", contexts)
                : "general professional communication";
        String goalsStr = goals != null && !goals.isEmpty()
                ? goals
                : "improve professional English communication";

        String topicOutline = batchTopics.stream()
                .map(topic -> "- " + topic.getName()
                        + " | " + topic.getDescription()
                        + " | level=" + topic.getLevel()
                        + " | imageKeyword=" + topic.getImageKeyword())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("- General topic");

        return """
                You are generating detailed English-learning content for a Vietnamese learner.
                
                Learner profile:
                - Role context: %s
                - Current level: %s
                - Communication contexts: %s
                - Goals: %s
                - Persona summary: %s
                
                Generate detailed learning content ONLY for these topic outlines:
                %s
                
                Output ONLY valid JSON:
                {
                  "topics": [
                    {
                      "name": "Topic in Vietnamese",
                      "situations": [
                        {
                          "title": "Situation in Vietnamese",
                          "description": "Context in Vietnamese",
                          "level": "BEGINNER",
                          "imageKeyword": "stock photo keyword",
                          "chunks": [
                            {
                              "contextQuestion": "English question",
                              "contextTranslation": "Vietnamese translation",
                              "rootSentence": "Pattern with ___.",
                              "rootTranslation": "Vietnamese translation",
                              "rootIpa": "/IPA/",
                              "rootDistractors": ["wrong root 1", "wrong root 2"],
                              "variableChunks": [
                                {
                                  "text": "chunk text",
                                  "translation": "Vietnamese meaning",
                                  "ipa": "/IPA/",
                                  "distractors": ["wrong 1", "wrong 2"],
                                  "imageKeyword": "search keyword"
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                
                Rules:
                1. Keep topics in the same order and with the same names as the outlines.
                2. Each topic should have 2-3 situations.
                3. CRITICAL: Each situation must have EXACTLY 5 chunks.
                4. Every chunk must have EXACTLY 3 variableChunks.
                5. Situations and descriptions should stay realistic to the learner profile.
                6. Topic names, situation titles, and descriptions must be in Vietnamese.
                7. Every rootSentence must contain EXACTLY ONE "___" blank.
                8. All 3 variableChunks in a chunk must fit that same single blank as interchangeable options.
                9. Each variableChunk.text must be a usable English phrase, usually 2-6 words, not a single isolated word.
                10. The 3 variableChunks in a chunk should feel complementary and useful for building the full sentence.
                11. Never output single-word variableChunks such as "meeting", "email", "plan", "team", or "client".
                12. Prefer reusable phrases like "in tomorrow's meeting", "with the client team", "for the next release", "before the deadline".
                13. Keep each variableChunk concise, natural, and directly usable in workplace communication.
                """.formatted(profession, level, contextStr, goalsStr, personaSummary, topicOutline);
    }

    private String callOpenRouter(String prompt, int maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "https://fluenz.com");
        headers.set("X-Title", "FluenZ");

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", maxTokens
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("Calling OpenRouter API: model={}, baseUrl={}", model, baseUrl);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/chat/completions",
                HttpMethod.POST,
                request,
                Map.class
        );
        log.info("OpenRouter API responded with status: {}", response.getStatusCode());

        if (response.getBody() == null) {
            throw new RuntimeException("Empty response from OpenRouter");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in OpenRouter response");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private LlmLearningPath parseLearningPathResponse(String content) throws JsonProcessingException {
        LlmLearningPath learningPath = objectMapper.readValue(stripMarkdownFences(content), LlmLearningPath.class);
        validateLearningPathResponse(learningPath);
        return learningPath;
    }

    private LlmBlueprint parseBlueprintResponse(String content) throws JsonProcessingException {
        return objectMapper.readValue(stripMarkdownFences(content), LlmBlueprint.class);
    }

    private String stripMarkdownFences(String content) {
        // Strip markdown code fences if present
        String json = content.trim();
        if (json.startsWith("```json")) {
            json = json.substring(7);
        } else if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        json = json.trim();
        return json;
    }

    private void validateLearningPathResponse(LlmLearningPath learningPath) {
        if (learningPath == null || learningPath.getTopics() == null || learningPath.getTopics().isEmpty()) {
            throw new RuntimeException("Learning path response contained no topics");
        }

        for (LlmTopic topic : learningPath.getTopics()) {
            if (topic.getSituations() == null || topic.getSituations().isEmpty()) {
                throw new RuntimeException("Topic '" + safeLabel(topic.getName()) + "' contained no situations");
            }

            for (LlmSituation situation : topic.getSituations()) {
                if (isBlank(situation.getImageKeyword())) {
                    situation.setImageKeyword(deriveSituationKeyword(situation));
                }

                if (situation.getChunks() == null || situation.getChunks().isEmpty()) {
                    throw new RuntimeException("Situation '" + safeLabel(situation.getTitle()) + "' contained no chunks");
                }
                if (situation.getChunks().size() < 5) {
                    throw new RuntimeException("Situation '" + safeLabel(situation.getTitle()) + "' must contain exactly 5 chunks");
                }
                if (situation.getChunks().size() > 5) {
                    situation.setChunks(new ArrayList<>(situation.getChunks().subList(0, 5)));
                }

                for (LlmChunk chunk : situation.getChunks()) {
                    chunk.setRootSentence(normalizeBlankToken(chunk.getRootSentence()));
                    chunk.setRootTranslation(normalizeBlankToken(chunk.getRootTranslation()));
                    chunk.setContextQuestion(trimToNull(chunk.getContextQuestion()));
                    chunk.setContextTranslation(trimToNull(chunk.getContextTranslation()));
                    chunk.setRootIpa(trimToNull(chunk.getRootIpa()));

                    int blankCount = countBlankSlots(chunk.getRootSentence());
                    if (blankCount != 1) {
                        throw new RuntimeException("Chunk rootSentence must contain exactly one blank, but got "
                                + blankCount + " in '" + safeLabel(chunk.getRootSentence()) + "'");
                    }

                    if (chunk.getVariableChunks() == null || chunk.getVariableChunks().size() != 3) {
                        throw new RuntimeException("Chunk must contain exactly 3 variableChunks");
                    }

                    List<LlmVariableChunk> cleanedChunks = new ArrayList<>();
                    for (LlmVariableChunk variableChunk : chunk.getVariableChunks()) {
                        if (isBlank(variableChunk.getText())) {
                            throw new RuntimeException("Variable chunk text was blank");
                        }

                        variableChunk.setText(normalizeVariableChunkText(variableChunk.getText(), chunk));
                        variableChunk.setTranslation(trimToNull(variableChunk.getTranslation()));
                        variableChunk.setIpa(trimToNull(variableChunk.getIpa()));

                        variableChunk.setDistractors(sanitizeDistractors(
                                variableChunk.getText(),
                                variableChunk.getDistractors()
                        ));

                        if (isBlank(variableChunk.getImageKeyword())) {
                            variableChunk.setImageKeyword(variableChunk.getText());
                        } else {
                            variableChunk.setImageKeyword(variableChunk.getImageKeyword().trim());
                        }

                        cleanedChunks.add(variableChunk);
                    }
                    chunk.setVariableChunks(cleanedChunks);
                }
            }
        }
    }

    private String deriveSituationKeyword(LlmSituation situation) {
        if (!isBlank(situation.getImageKeyword())) {
            return situation.getImageKeyword().trim();
        }
        if (situation.getChunks() != null) {
            for (LlmChunk chunk : situation.getChunks()) {
                if (!isBlank(chunk.getContextQuestion())) {
                    return chunk.getContextQuestion().trim();
                }
                if (!isBlank(chunk.getRootSentence())) {
                    return chunk.getRootSentence().replace("___", "").trim();
                }
            }
        }
        return trimToNull(situation.getTitle());
    }

    private String normalizeBlankToken(String value) {
        if (value == null) {
            return null;
        }
        return value
                .replaceAll("_{2,}", "___")
                .replaceAll("\\s*___\\s*", " ___ ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int countBlankSlots(String value) {
        if (isBlank(value)) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = value.indexOf("___", index)) >= 0) {
            count++;
            index += 3;
        }
        return count;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String safeLabel(String value) {
        return value == null ? "(blank)" : value;
    }

    private List<String> sanitizeDistractors(String correctText, List<String> distractors) {
        List<String> cleaned = new ArrayList<>();
        if (distractors != null) {
            for (String distractor : distractors) {
                String normalized = trimToNull(distractor);
                if (normalized == null || normalized.equalsIgnoreCase(correctText) || cleaned.contains(normalized)) {
                    continue;
                }
                cleaned.add(normalized);
                if (cleaned.size() == 2) {
                    break;
                }
            }
        }

        List<String> fallbacks = buildFallbackDistractors(correctText);
        for (String fallback : fallbacks) {
            if (!cleaned.contains(fallback) && !fallback.equalsIgnoreCase(correctText)) {
                cleaned.add(fallback);
            }
            if (cleaned.size() == 2) {
                break;
            }
        }

        if (cleaned.size() < 2) {
            cleaned.add("general option");
        }
        if (cleaned.size() < 2) {
            cleaned.add("another choice");
        }

        return cleaned.subList(0, 2);
    }

    private List<String> buildFallbackDistractors(String correctText) {
        String normalized = correctText == null ? "" : correctText.trim();
        if (normalized.isBlank()) {
            return List.of("general option", "another choice");
        }

        List<String> fallbacks = new ArrayList<>();
        if (normalized.contains(" ")) {
            fallbacks.add("different " + normalized.substring(normalized.indexOf(' ') + 1));
            fallbacks.add("another " + normalized.substring(normalized.indexOf(' ') + 1));
        } else {
            fallbacks.add(normalized + " option");
            fallbacks.add("basic " + normalized);
        }
        fallbacks.add("general option");
        fallbacks.add("another choice");
        return fallbacks;
    }

    private int countWords(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return 0;
        }

        return (int) java.util.Arrays.stream(normalized.split("\\s+"))
                .map(token -> token.replaceAll("^[^A-Za-z0-9]+|[^A-Za-z0-9]+$", ""))
                .filter(token -> !token.isBlank())
                .count();
    }

    private String normalizeVariableChunkText(String rawText, LlmChunk chunk) {
        String normalized = trimToNull(rawText);
        if (normalized == null) {
            throw new RuntimeException("Variable chunk text was blank");
        }

        if (countWords(normalized) >= 2) {
            return normalized;
        }

        String repaired = repairSingleWordChunk(normalized, chunk);
        log.warn("Repaired single-word variable chunk '{}' -> '{}'", normalized, repaired);
        return repaired;
    }

    private String repairSingleWordChunk(String rawWord, LlmChunk chunk) {
        String word = rawWord.trim();
        String lowerWord = word.toLowerCase(Locale.ROOT);
        String rootSentence = chunk != null ? trimToNull(chunk.getRootSentence()) : null;
        String contextQuestion = chunk != null ? trimToNull(chunk.getContextQuestion()) : null;
        String cue = ((rootSentence == null ? "" : rootSentence) + " " + (contextQuestion == null ? "" : contextQuestion))
                .toLowerCase(Locale.ROOT);

        if (cue.contains("with") || cue.contains("work with") || cue.contains("coordinate") || cue.contains("collaborate")) {
            return "with " + lowerWord;
        }
        if (cue.contains("about") || cue.contains("discuss") || cue.contains("talk")) {
            return "about " + lowerWord;
        }
        if (cue.contains("for") || cue.contains("plan") || cue.contains("prepare") || cue.contains("deadline")) {
            return "for " + lowerWord;
        }
        if (cue.contains("in") || cue.contains("meeting") || cue.contains("call") || cue.contains("interview")) {
            return "in the " + lowerWord;
        }

        return "about " + lowerWord;
    }
}
