package com.fluenz.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class ImageService {

    @Value("${unsplash.access.key:}")
    private String unsplashAccessKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetch a stock image URL from Unsplash based on keyword search.
     * Returns null gracefully on any failure (rate limit, network, missing key).
     */
    public String fetchImageUrl(String keyword) {
        if (unsplashAccessKey == null || unsplashAccessKey.isBlank()) {
            log.debug("Unsplash API key not configured, skipping image fetch");
            return null;
        }

        try {
            String encodedQuery = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://api.unsplash.com/search/photos?query=" + encodedQuery
                    + "&per_page=1&orientation=landscape&content_filter=high";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Client-ID " + unsplashAccessKey)
                    .header("Accept-Version", "v1")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Unsplash API error: status={}, body={}", response.statusCode(), response.body().substring(0, Math.min(200, response.body().length())));
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode results = root.get("results");
            if (results != null && results.isArray() && !results.isEmpty()) {
                JsonNode urls = results.get(0).get("urls");
                if (urls != null && urls.has("small")) {
                    return urls.get("small").asText();
                }
            }

            log.debug("No Unsplash results found for keyword: {}", keyword);
            return null;
        } catch (Exception e) {
            log.warn("Failed to fetch image from Unsplash for keyword '{}': {}", keyword, e.getMessage());
            return null;
        }
    }
}
