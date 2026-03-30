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
public class PexelsImageProvider {

    @Value("${pexels.access.key:}")
    private String pexelsApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getProviderName() {
        return "pexels";
    }

    public boolean isConfigured() {
        return pexelsApiKey != null && !pexelsApiKey.isBlank();
    }

    public String fetchImageUrl(String keyword) {
        if (!isConfigured()) {
            log.debug("Pexels API key not configured, skipping");
            return null;
        }

        try {
            String encodedQuery = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://api.pexels.com/v1/search?query=" + encodedQuery
                    + "&per_page=1&orientation=landscape";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", pexelsApiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Pexels API error: status={}", response.statusCode());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode photos = root.get("photos");
            if (photos != null && photos.isArray() && !photos.isEmpty()) {
                JsonNode src = photos.get(0).get("src");
                if (src != null && src.has("medium")) {
                    return src.get("medium").asText();
                }
            }

            log.debug("No Pexels results found for keyword: {}", keyword);
            return null;
        } catch (Exception e) {
            log.warn("Failed to fetch image from Pexels for keyword '{}': {}", keyword, e.getMessage());
            return null;
        }
    }
}
