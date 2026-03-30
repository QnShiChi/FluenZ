package com.fluenz.api.service;

import com.fluenz.api.entity.LearningPath;
import com.fluenz.api.entity.Situation;
import com.fluenz.api.entity.SubPhrase;
import com.fluenz.api.repository.LearningPathRepository;
import com.fluenz.api.repository.SituationRepository;
import com.fluenz.api.repository.SubPhraseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class ImagePopulationService {

    private final LearningPathRepository learningPathRepository;
    private final SituationRepository situationRepository;
    private final SubPhraseRepository subPhraseRepository;
    private final ImageService imageService;
    private final Executor imagePopulationExecutor;
    private final TransactionTemplate txTemplate;
    private final TransactionTemplate txReadOnly;

    public ImagePopulationService(
            LearningPathRepository learningPathRepository,
            SituationRepository situationRepository,
            SubPhraseRepository subPhraseRepository,
            ImageService imageService,
            @Qualifier("imagePopulationExecutor") Executor imagePopulationExecutor,
            PlatformTransactionManager txManager
    ) {
        this.learningPathRepository = learningPathRepository;
        this.situationRepository = situationRepository;
        this.subPhraseRepository = subPhraseRepository;
        this.imageService = imageService;
        this.imagePopulationExecutor = imagePopulationExecutor;

        this.txTemplate = new TransactionTemplate(txManager);
        this.txReadOnly = new TransactionTemplate(txManager);
        this.txReadOnly.setReadOnly(true);
    }

    /**
     * Entry point: runs on the async executor thread.
     * Step 1: Load entity graph in a programmatic transaction to collect IDs + keywords.
     * Step 2: Fetch images in parallel (no transaction needed for HTTP calls).
     * Step 3: Save each result individually in its own transaction.
     */
    @Async("imagePopulationExecutor")
    public void populateImagesAsync(UUID learningPathId) {
        log.info("Starting async image population for learning path: {}", learningPathId);

        try {
            // Step 1: Collect all image tasks (IDs + keywords) within a programmatic transaction
            List<ImageTask> tasks = txReadOnly.execute(status -> {
                LearningPath path = learningPathRepository.findById(learningPathId).orElse(null);
                if (path == null) {
                    log.warn("Learning path not found for image population: {}", learningPathId);
                    return List.<ImageTask>of();
                }

                List<ImageTask> collected = new ArrayList<>();

                path.getTopics().forEach(topic ->
                    topic.getSituations().forEach(situation -> {
                        if (situation.getImageKeyword() != null && !situation.getImageKeyword().isBlank()) {
                            collected.add(new ImageTask(situation.getId(), situation.getImageKeyword(), ImageTaskType.SITUATION));
                        }

                        situation.getChunks().forEach(chunk ->
                            chunk.getSubPhrases().forEach(subPhrase -> {
                                if (subPhrase.getImageKeyword() != null && !subPhrase.getImageKeyword().isBlank()) {
                                    collected.add(new ImageTask(subPhrase.getId(), subPhrase.getImageKeyword(), ImageTaskType.SUB_PHRASE));
                                }
                            })
                        );
                    })
                );

                return collected;
            });

            if (tasks == null || tasks.isEmpty()) {
                log.info("No image tasks found for learning path: {}", learningPathId);
                return;
            }

            log.info("Collected {} image tasks for path: {}", tasks.size(), learningPathId);

            // Step 2: Fetch images in parallel (pure HTTP, no DB session needed)
            List<CompletableFuture<Void>> futures = tasks.stream()
                    .map(task -> CompletableFuture.runAsync(() -> {
                        try {
                            String imageUrl = imageService.fetchImageUrl(task.keyword);
                            if (imageUrl != null) {
                                task.resolvedUrl = imageUrl;
                                log.info("Fetched image for {} '{}': {}", task.type, task.keyword, imageUrl.substring(0, Math.min(80, imageUrl.length())));
                            } else {
                                log.warn("No image found for {} '{}'", task.type, task.keyword);
                            }
                        } catch (Exception e) {
                            log.warn("Error fetching image for '{}': {}", task.keyword, e.getMessage());
                        }
                    }, imagePopulationExecutor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Step 3: Save resolved URLs back to DB in transactions
            int saved = 0;
            for (ImageTask task : tasks) {
                if (task.resolvedUrl != null) {
                    try {
                        txTemplate.executeWithoutResult(status -> {
                            if (task.type == ImageTaskType.SITUATION) {
                                situationRepository.findById(task.entityId).ifPresent(situation -> {
                                    situation.setThumbnailUrl(task.resolvedUrl);
                                    situationRepository.save(situation);
                                    log.debug("Saved thumbnail for situation: {}", task.entityId);
                                });
                            } else {
                                subPhraseRepository.findById(task.entityId).ifPresent(subPhrase -> {
                                    subPhrase.setImageUrl(task.resolvedUrl);
                                    subPhraseRepository.save(subPhrase);
                                    log.debug("Saved image for subPhrase: {}", task.entityId);
                                });
                            }
                        });
                        saved++;
                    } catch (Exception e) {
                        log.warn("Failed to save image for {} {}: {}", task.type, task.entityId, e.getMessage());
                    }
                }
            }

            log.info("Completed image population for path: {}. Saved {}/{} images.",
                    learningPathId, saved, tasks.size());

        } catch (Exception e) {
            log.error("Error during async image population for path {}: {}", learningPathId, e.getMessage(), e);
        }
    }

    // --- Inner types ---

    enum ImageTaskType { SITUATION, SUB_PHRASE }

    static class ImageTask {
        final UUID entityId;
        final String keyword;
        final ImageTaskType type;
        volatile String resolvedUrl;

        ImageTask(UUID entityId, String keyword, ImageTaskType type) {
            this.entityId = entityId;
            this.keyword = keyword;
            this.type = type;
        }
    }
}
