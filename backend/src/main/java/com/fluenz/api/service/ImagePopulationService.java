package com.fluenz.api.service;

import com.fluenz.api.entity.LearningPath;
import com.fluenz.api.entity.Situation;
import com.fluenz.api.entity.SubPhrase;
import com.fluenz.api.repository.LearningPathRepository;
import com.fluenz.api.repository.SituationRepository;
import com.fluenz.api.repository.SubPhraseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ImagePopulationService {

    private static final List<String> GENERIC_PROFESSIONAL_QUERIES = List.of(
            "person working on laptop",
            "business meeting",
            "office discussion",
            "presentation in office",
            "team collaboration office",
            "professional portrait office"
    );

    private static final List<FallbackPalette> FALLBACK_PALETTES = List.of(
            new FallbackPalette("#0F172A", "#2563EB", "#E0F2FE"),
            new FallbackPalette("#1F2937", "#0EA5E9", "#E0F7FF"),
            new FallbackPalette("#111827", "#14B8A6", "#DCFCE7"),
            new FallbackPalette("#1E293B", "#F59E0B", "#FEF3C7"),
            new FallbackPalette("#172554", "#7C3AED", "#EDE9FE"),
            new FallbackPalette("#312E81", "#EC4899", "#FCE7F3")
    );

    private final LearningPathRepository learningPathRepository;
    private final SituationRepository situationRepository;
    private final SubPhraseRepository subPhraseRepository;
    private final ImageService imageService;
    private final GenerationProgressService generationProgressService;
    private final TransactionTemplate txTemplate;
    private final TransactionTemplate txReadOnly;

    public ImagePopulationService(
            LearningPathRepository learningPathRepository,
            SituationRepository situationRepository,
            SubPhraseRepository subPhraseRepository,
            ImageService imageService,
            GenerationProgressService generationProgressService,
            PlatformTransactionManager txManager
    ) {
        this.learningPathRepository = learningPathRepository;
        this.situationRepository = situationRepository;
        this.subPhraseRepository = subPhraseRepository;
        this.imageService = imageService;
        this.generationProgressService = generationProgressService;

        this.txTemplate = new TransactionTemplate(txManager);
        this.txReadOnly = new TransactionTemplate(txManager);
        this.txReadOnly.setReadOnly(true);
    }

    @Async("imagePopulationExecutor")
    public void populateImagesAsync(UUID learningPathId) {
        log.info("Starting async image population for learning path: {}", learningPathId);

        try {
            PathImageTasks tasks = txReadOnly.execute(status -> collectTasks(learningPathId));
            if (tasks == null) {
                generationProgressService.complete(learningPathId, "Personalized path is ready.");
                return;
            }

            hydrateSituationThumbnails(tasks.situationTasks);
            hydrateSubPhraseImages(tasks.subPhraseTasks);

            generationProgressService.complete(
                    learningPathId,
                    "Personalized path is ready with hydrated thumbnails."
            );
            log.info("Completed image population for learning path: {}", learningPathId);
        } catch (Exception e) {
            log.error("Error during async image population for path {}: {}", learningPathId, e.getMessage(), e);
            generationProgressService.complete(
                    learningPathId,
                    "Personalized path is ready. Some thumbnails used fallback assets."
            );
        }
    }

    private PathImageTasks collectTasks(UUID learningPathId) {
        LearningPath path = learningPathRepository.findById(learningPathId).orElse(null);
        if (path == null) {
            log.warn("Learning path not found for image population: {}", learningPathId);
            return null;
        }

        List<SituationImageTask> situationTasks = new ArrayList<>();
        List<SubPhraseImageTask> subPhraseTasks = new ArrayList<>();

        path.getTopics().forEach(topic ->
                topic.getSituations().forEach(situation -> {
                    if (situation.getThumbnailUrl() == null || situation.getThumbnailUrl().isBlank()) {
                        situationTasks.add(new SituationImageTask(
                                situation.getId(),
                                trimToNull(situation.getImageKeyword()),
                                trimToNull(situation.getTitle()),
                                trimToNull(situation.getDescription()),
                                trimToNull(topic.getName()),
                                situation.getOrderIndex() == null ? 0 : situation.getOrderIndex()
                        ));
                    }

                    situation.getChunks().forEach(chunk ->
                            chunk.getSubPhrases().forEach(subPhrase -> {
                                if (subPhrase.getImageUrl() == null || subPhrase.getImageUrl().isBlank()) {
                                    String keyword = resolveSubPhraseKeyword(subPhrase);
                                    if (keyword != null) {
                                        subPhraseTasks.add(new SubPhraseImageTask(subPhrase.getId(), keyword));
                                    }
                                }
                            })
                    );
                })
        );

        return new PathImageTasks(situationTasks, subPhraseTasks);
    }

    private void hydrateSituationThumbnails(List<SituationImageTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        Set<String> usedFingerprints = new LinkedHashSet<>();
        Set<String> usedQueries = new LinkedHashSet<>();

        for (int index = 0; index < tasks.size(); index++) {
            SituationImageTask task = tasks.get(index);
            SituationResolution resolution = resolveSituationImage(task, index, usedFingerprints, usedQueries);
            usedFingerprints.add(resolution.fingerprint);

            txTemplate.executeWithoutResult(status ->
                    situationRepository.findById(task.entityId).ifPresent(situation -> {
                        situation.setThumbnailUrl(resolution.url);
                        situation.setThumbnailQuery(resolution.primaryQuery);
                        situation.setFallbackQuery(resolution.fallbackQuery);
                        situation.setThumbnailSource(resolution.source);
                        situation.setImageFingerprint(resolution.fingerprint);
                        situation.setAssetRetryCount(resolution.retryCount);
                        situation.setGenerationStatus("READY");
                        if (situation.getContentValidationStatus() == null || situation.getContentValidationStatus().isBlank()) {
                            situation.setContentValidationStatus("VALIDATED");
                        }
                        situationRepository.save(situation);
                    })
            );
        }
    }

    private SituationResolution resolveSituationImage(
            SituationImageTask task,
            int taskIndex,
            Set<String> usedFingerprints,
            Set<String> usedQueries
    ) {
        List<String> candidates = buildSituationQueries(task, taskIndex);
        String firstQuery = candidates.isEmpty() ? null : candidates.get(0);
        String lastTriedQuery = firstQuery;
        int retryCount = 0;

        for (String candidate : candidates) {
            String normalizedCandidate = normalizeValue(candidate);
            if (normalizedCandidate.isBlank() || !usedQueries.add(normalizedCandidate)) {
                continue;
            }

            retryCount++;
            lastTriedQuery = candidate;

            try {
                String imageUrl = imageService.fetchImageUrl(candidate);
                String fingerprint = normalizeFingerprint(imageUrl);
                if (imageUrl == null || fingerprint == null || usedFingerprints.contains(fingerprint)) {
                    continue;
                }

                return new SituationResolution(
                        imageUrl,
                        firstQuery,
                        candidate.equals(firstQuery) ? null : candidate,
                        sourceFromUrl(imageUrl),
                        fingerprint,
                        retryCount
                );
            } catch (Exception e) {
                log.warn("Failed to fetch thumbnail for situation {} with query '{}': {}",
                        task.entityId, candidate, e.getMessage());
            }
        }

        String fallbackLabel = buildFallbackLabel(task);
        String fallbackUrl = buildSvgFallback(fallbackLabel, taskIndex);
        return new SituationResolution(
                fallbackUrl,
                firstQuery,
                lastTriedQuery != null && !lastTriedQuery.equals(firstQuery) ? lastTriedQuery : "internal curated fallback",
                "internal_fallback",
                normalizeFingerprint(fallbackUrl),
                retryCount
        );
    }

    private void hydrateSubPhraseImages(List<SubPhraseImageTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        Map<String, String> resolvedByKeyword = new LinkedHashMap<>();
        for (SubPhraseImageTask task : tasks) {
            String normalizedKeyword = normalizeValue(task.keyword);
            if (!resolvedByKeyword.containsKey(normalizedKeyword)) {
                try {
                    resolvedByKeyword.put(normalizedKeyword, imageService.fetchImageUrl(task.keyword));
                } catch (Exception e) {
                    resolvedByKeyword.put(normalizedKeyword, null);
                    log.warn("Failed subphrase image fetch for '{}': {}", task.keyword, e.getMessage());
                }
            }

            String imageUrl = resolvedByKeyword.get(normalizedKeyword);
            if (imageUrl == null) {
                continue;
            }

            txTemplate.executeWithoutResult(status ->
                    subPhraseRepository.findById(task.entityId).ifPresent(subPhrase -> {
                        subPhrase.setImageUrl(imageUrl);
                        subPhraseRepository.save(subPhrase);
                    })
            );
        }
    }

    private List<String> buildSituationQueries(SituationImageTask task, int taskIndex) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        addCandidate(candidates, task.exactKeyword);

        String semanticQuery = joinParts(task.title, task.topicName, "professional communication");
        addCandidate(candidates, semanticQuery);

        String contextQuery = joinParts(task.title, task.description, "office");
        addCandidate(candidates, shortenQuery(contextQuery));

        String roleIndustryQuery = joinParts(task.topicName, "business conversation");
        addCandidate(candidates, roleIndustryQuery);

        addCandidate(candidates, GENERIC_PROFESSIONAL_QUERIES.get(taskIndex % GENERIC_PROFESSIONAL_QUERIES.size()));
        addCandidate(candidates, GENERIC_PROFESSIONAL_QUERIES.get((taskIndex + 2) % GENERIC_PROFESSIONAL_QUERIES.size()));

        return new ArrayList<>(candidates);
    }

    private void addCandidate(Set<String> candidates, String value) {
        String normalized = normalizeValue(value);
        if (!normalized.isBlank()) {
            candidates.add(value.trim());
        }
    }

    private String joinParts(String... parts) {
        List<String> kept = new ArrayList<>();
        for (String part : parts) {
            String normalized = trimToNull(part);
            if (normalized != null) {
                kept.add(normalized);
            }
        }
        return kept.isEmpty() ? null : String.join(" ", kept);
    }

    private String shortenQuery(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        String[] tokens = normalized.split("\\s+");
        if (tokens.length <= 6) {
            return normalized;
        }
        return String.join(" ", java.util.Arrays.copyOf(tokens, 6));
    }

    private String buildFallbackLabel(SituationImageTask task) {
        if (task.title != null && !task.title.isBlank()) {
            return task.title;
        }
        if (task.topicName != null && !task.topicName.isBlank()) {
            return task.topicName;
        }
        return "Professional Scenario";
    }

    private String buildSvgFallback(String label, int index) {
        FallbackPalette palette = FALLBACK_PALETTES.get(index % FALLBACK_PALETTES.size());
        String safeLabel = escapeSvg(label);
        String svg = """
                <svg xmlns='http://www.w3.org/2000/svg' width='1200' height='760' viewBox='0 0 1200 760'>
                  <defs>
                    <linearGradient id='g' x1='0' y1='0' x2='1' y2='1'>
                      <stop offset='0%%' stop-color='%s'/>
                      <stop offset='100%%' stop-color='%s'/>
                    </linearGradient>
                  </defs>
                  <rect width='1200' height='760' rx='48' fill='url(#g)'/>
                  <circle cx='970' cy='164' r='110' fill='rgba(255,255,255,0.10)'/>
                  <circle cx='196' cy='612' r='140' fill='rgba(255,255,255,0.08)'/>
                  <rect x='78' y='86' width='274' height='38' rx='19' fill='rgba(255,255,255,0.16)'/>
                  <text x='92' y='112' fill='white' font-size='18' font-family='Arial, sans-serif'>FLUENZ PERSONALIZED</text>
                  <text x='78' y='352' fill='white' font-size='56' font-weight='700' font-family='Arial, sans-serif'>%s</text>
                  <text x='78' y='410' fill='%s' font-size='24' font-family='Arial, sans-serif'>Professional communication thumbnail</text>
                </svg>
                """.formatted(palette.backgroundStart, palette.backgroundEnd, safeLabel, palette.accentText);
        String encoded = java.net.URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
        return "data:image/svg+xml;charset=UTF-8," + encoded;
    }

    private String sourceFromUrl(String url) {
        String normalized = normalizeValue(url);
        if (normalized.contains("unsplash")) {
            return "unsplash";
        }
        if (normalized.contains("pexels")) {
            return "pexels";
        }
        if (normalized.contains("pixabay")) {
            return "pixabay";
        }
        if (normalized.startsWith("data:image/svg+xml")) {
            return "internal_fallback";
        }
        return "external";
    }

    private String normalizeFingerprint(String value) {
        String normalized = normalizeValue(value);
        return normalized.isBlank() ? null : normalized;
    }

    private String resolveSubPhraseKeyword(SubPhrase subPhrase) {
        if (subPhrase.getImageKeyword() != null && !subPhrase.getImageKeyword().isBlank()) {
            return subPhrase.getImageKeyword().trim();
        }
        if (subPhrase.getText() != null && !subPhrase.getText().isBlank()) {
            return subPhrase.getText().trim();
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeValue(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String escapeSvg(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private record PathImageTasks(List<SituationImageTask> situationTasks, List<SubPhraseImageTask> subPhraseTasks) {}

    private record SituationImageTask(
            UUID entityId,
            String exactKeyword,
            String title,
            String description,
            String topicName,
            int orderIndex
    ) {}

    private record SubPhraseImageTask(UUID entityId, String keyword) {}

    private record SituationResolution(
            String url,
            String primaryQuery,
            String fallbackQuery,
            String source,
            String fingerprint,
            int retryCount
    ) {}

    private record FallbackPalette(String backgroundStart, String backgroundEnd, String accentText) {}
}
