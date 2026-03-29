package com.fluenz.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class RoleplayService {

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    @Value("${openrouter.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public RoleplayService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * Generate AI response for roleplay chat.
     *
     * @param situationTitle   e.g. "Discussing login feature delay"
     * @param situationDesc    e.g. "A project standup meeting"
     * @param targetChunks     e.g. ["due to new requirements", "because of user feedback"]
     * @param chatHistory      list of {role, content} maps
     * @param turnNumber       1 = AI acknowledges user, 2 = AI answers + evaluates
     */
    public String chat(String situationTitle, String situationDesc,
                       List<String> targetChunks, List<Map<String, String>> chatHistory,
                       int turnNumber) {

        String systemPrompt = buildSystemPrompt(situationTitle, situationDesc, targetChunks, turnNumber);

        // Build messages: system + chat history
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(chatHistory);

        return callLlm(messages);
    }

    private String buildSystemPrompt(String situationTitle, String situationDesc,
                                      List<String> targetChunks, int turnNumber) {
        String chunksStr = String.join(", ", targetChunks.stream()
                .map(c -> "\"" + c + "\"").toList());

        String coreContext = """
                You are having a work conversation with an English learner.
                
                SITUATION: %s
                DESCRIPTION: %s
                
                The learner has been practicing these TARGET PHRASES: [%s].
                Recognize if they use these phrases correctly.
                
                IMPORTANT RULES:
                - Use simple English (A2-B1 level vocabulary)
                - Be natural and conversational
                - Keep responses SHORT
                """.formatted(situationTitle, situationDesc, chunksStr);

        String turnRule;
        if (turnNumber < 2) {
            turnRule = """
                    THIS IS AN INTERMEDIATE TURN — You are responding to the user.
                    
                    STRICT RULES FOR THIS TURN:
                    - Respond naturally to what the user just said
                    - Keep your response to MAX 2 short sentences
                    - You CAN ask a brief follow-up question or make an encouraging statement to keep the conversation going
                    - Do NOT evaluate or correct the user in this turn
                    """;
        } else {
            turnRule = """
                    THIS IS THE FINAL TURN — You are answering the user's last statement/question and evaluating.
                    
                    STRICT RULES FOR THIS TURN:
                    1. You MUST answer the user's LAST question first in 1-2 natural English sentences.
                    2. Do NOT skip the answer.
                    3. Do NOT start with feedback or evaluation.
                    4. The first line of your response MUST be the English answer only.
                    5. Then add a line break and write "---"
                    6. Then provide an EVALUATION BLOCK entirely in Vietnamese:
                       - Did they use any Target Phrases? Which ones?
                       - Correct any grammar mistakes from their messages
                       - Give a brief, encouraging summary of their performance
                       - Do NOT use emoji
                       - Keep the tone professional, supportive, and concise
                    7. If you do not answer the user's last question before the separator, your response is invalid.
                    
                    Example format:
                    That sounds like a solid plan for next sprint.
                    
                    ---
                    
                    Danh gia:
                    Ban da su dung cum "due to new requirements" kha tu nhien.
                    Luu y: "I working on" nen doi thanh "I'm working on" hoac "I was working on".
                    Tong ket: Ban giao tiep kha ro rang va da co gang dua cum tu da hoc vao dung ngu canh.
                    """;
        }

        return coreContext + "\n" + turnRule;
    }

    private String callLlm(List<Map<String, String>> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "https://fluenz.com");
        headers.set("X-Title", "FluenZ");

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0.7,
                "max_tokens", 1024
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("Roleplay LLM call: model={}, messages={}", model, messages.size());
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/chat/completions",
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Empty response from LLM");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in LLM response");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        log.info("Roleplay LLM response length: {}", content.length());
        return content.trim();
    }
}
