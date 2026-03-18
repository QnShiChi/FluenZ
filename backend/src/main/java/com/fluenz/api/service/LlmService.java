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
import java.util.List;
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
    public static class LlmTopic {
        private String name;
        private List<LlmSituation> situations;
    }

    @Data
    public static class LlmSituation {
        private String title;
        private String description;
        private String level;
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
        String prompt = buildPrompt(profession, level, contexts, goals);

        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                String response = callOpenRouter(prompt);
                return parseResponse(response);
            } catch (Exception e) {
                log.warn("LLM attempt {} failed: {}", attempt + 1, e.getMessage());
                if (attempt == 2) {
                    throw new RuntimeException("Failed to generate learning path after 3 attempts", e);
                }
            }
        }
        throw new RuntimeException("Unreachable");
    }

    private String buildPrompt(String profession, String level, List<String> contexts, String goals) {
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
                4. contextQuestion must be a natural English question
                5. rootSentence must contain "___" where variable chunks slot in
                6. Each variableChunk.text is a short English phrase (2-6 words)
                7. Each variableChunk.translation is the Vietnamese meaning of variableChunk.text
                8. Each variableChunk.distractors has exactly 2 wrong but plausible alternatives
                9. rootDistractors has exactly 2 wrong but plausible alternatives for the root sentence
                10. IPA must be accurate International Phonetic Alphabet transcription
                11. Each variableChunk.imageKeyword is a 2-4 word English phrase suitable for stock photo search (e.g., "team meeting whiteboard", "user feedback laptop")
                12. CRITICAL: You MUST generate EXACTLY 3 variableChunks for each chunk. Not 2, not 4 — exactly 3.
                
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

    private String callOpenRouter(String prompt) {
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
                "max_tokens", 8192
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

    private LlmLearningPath parseResponse(String content) throws JsonProcessingException {
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

        return objectMapper.readValue(json, LlmLearningPath.class);
    }
}
