package com.fluenz.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluenz.api.entity.ImageCache;
import com.fluenz.api.repository.ImageCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    @Value("${unsplash.access.key:}")
    private String unsplashAccessKey;

    private final ImageCacheRepository imageCacheRepository;
    private final PexelsImageProvider pexelsImageProvider;
    private final PixabayImageProvider pixabayImageProvider;
    private final TransactionTemplate txTemplate;
    private final TransactionTemplate txReadOnly;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageService(ImageCacheRepository imageCacheRepository,
                        PexelsImageProvider pexelsImageProvider,
                        PixabayImageProvider pixabayImageProvider,
                        PlatformTransactionManager txManager) {
        this.imageCacheRepository = imageCacheRepository;
        this.pexelsImageProvider = pexelsImageProvider;
        this.pixabayImageProvider = pixabayImageProvider;

        this.txTemplate = new TransactionTemplate(txManager);
        this.txReadOnly = new TransactionTemplate(txManager);
        this.txReadOnly.setReadOnly(true);
    }

    /**
     * Fetch an image URL using the chain: Cache → Unsplash → Pexels → Pixabay.
     * Thread-safe — uses programmatic transactions instead of @Transactional.
     */
    public String fetchImageUrl(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();

        // 1. Check cache first (programmatic read-only transaction)
        try {
            String cached = txReadOnly.execute(status -> {
                Optional<ImageCache> entry = imageCacheRepository.findValidByKeyword(normalizedKeyword);
                return entry.map(ImageCache::getImageUrl).orElse(null);
            });
            if (cached != null) {
                log.debug("Cache hit for keyword: {}", normalizedKeyword);
                return cached;
            }
        } catch (Exception e) {
            log.debug("Cache check failed, proceeding to providers: {}", e.getMessage());
        }

        // 2. Try provider chain
        String imageUrl = fetchFromUnsplash(normalizedKeyword);
        String provider = "unsplash";

        if (imageUrl == null) {
            imageUrl = pexelsImageProvider.fetchImageUrl(normalizedKeyword);
            provider = "pexels";
        }

        if (imageUrl == null) {
            imageUrl = pixabayImageProvider.fetchImageUrl(normalizedKeyword);
            provider = "pixabay";
        }

        // 3. Cache the result if found (programmatic write transaction)
        if (imageUrl != null) {
            final String urlToCache = imageUrl;
            final String providerName = provider;
            try {
                txTemplate.executeWithoutResult(status -> {
                    LocalDateTime now = LocalDateTime.now();
                    imageCacheRepository.upsert(
                            UUID.randomUUID(),
                            normalizedKeyword,
                            urlToCache,
                            providerName,
                            now,
                            now.plusDays(30)
                    );
                });
            } catch (Exception e) {
                log.debug("Failed to cache image, continuing: {}", e.getMessage());
            }
            log.info("Image fetched from {} for keyword: {}", provider, normalizedKeyword);
        } else {
            log.warn("No image found from any provider for keyword: {}", normalizedKeyword);
        }

        return imageUrl;
    }

    private String fetchFromUnsplash(String keyword) {
        if (unsplashAccessKey == null || unsplashAccessKey.isBlank()) {
            log.debug("Unsplash API key not configured, skipping");
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
                log.warn("Unsplash API error: status={}", response.statusCode());
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
