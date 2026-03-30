package com.fluenz.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class PixabayImageProvider {

    @Value("${pixabay.access.key:}")
    private String pixabayApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getProviderName() {
        return "pixabay";
    }

    public boolean isConfigured() {
        return pixabayApiKey != null && !pixabayApiKey.isBlank();
    }

    public String fetchImageUrl(String keyword) {
        if (!isConfigured()) {
            log.debug("Pixabay API key not configured, skipping");
            return null;
        }

        try {
            String encodedQuery = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://pixabay.com/api/?key=" + pixabayApiKey
                    + "&q=" + encodedQuery
                    + "&per_page=3&image_type=photo&orientation=horizontal&safesearch=true";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Pixabay API error: status={}", response.statusCode());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode hits = root.get("hits");
            if (hits != null && hits.isArray() && !hits.isEmpty()) {
                JsonNode firstHit = hits.get(0);
                if (firstHit.has("webformatURL")) {
                    return firstHit.get("webformatURL").asText();
                }
            }

            log.debug("No Pixabay results found for keyword: {}", keyword);
            return null;
        } catch (Exception e) {
            log.warn("Failed to fetch image from Pixabay for keyword '{}': {}", keyword, e.getMessage());
            return null;
        }
    }
}
