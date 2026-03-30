package com.fluenz.api.service;

import com.fluenz.api.dto.response.GenerationProgressResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GenerationProgressService {

    private final Map<UUID, GenerationProgressResponse> progressMap = new ConcurrentHashMap<>();

    public GenerationProgressResponse start(UUID pathId, String statusText) {
        GenerationProgressResponse response = GenerationProgressResponse.builder()
                .pathId(pathId)
                .phase("QUEUED")
                .statusText(statusText)
                .currentBatch(0)
                .totalBatches(0)
                .completedTopics(0)
                .totalTopics(0)
                .currentTopicName(null)
                .progressPercent(5)
                .complete(false)
                .failed(false)
                .errorMessage(null)
                .build();
        progressMap.put(pathId, response);
        return response;
    }

    public void markBlueprint(UUID pathId, String statusText) {
        mutate(pathId, builder -> builder
                .phase("BLUEPRINT")
                .statusText(statusText)
                .progressPercent(15)
                .failed(false)
                .errorMessage(null));
    }

    public void markBlueprintReady(UUID pathId, int totalTopics, int totalBatches, String nextTopicName) {
        mutate(pathId, builder -> builder
                .phase("DETAILS")
                .statusText("Blueprint ready. Starting topic batches.")
                .totalTopics(totalTopics)
                .totalBatches(totalBatches)
                .currentTopicName(nextTopicName)
                .progressPercent(Math.min(25, totalTopics > 0 ? 20 : 15)));
    }

    public void markBatch(UUID pathId, int currentBatch, int totalBatches, int completedTopics, int totalTopics, String currentTopicName) {
        int batchProgress = totalTopics <= 0 ? 25 : 25 + (int) Math.round((completedTopics * 55.0) / totalTopics);
        mutate(pathId, builder -> builder
                .phase("DETAILS")
                .statusText("Generating topic batch " + currentBatch + " of " + totalBatches + ".")
                .currentBatch(currentBatch)
                .totalBatches(totalBatches)
                .completedTopics(completedTopics)
                .totalTopics(totalTopics)
                .currentTopicName(currentTopicName)
                .progressPercent(Math.min(80, Math.max(25, batchProgress))));
    }

    public void markFinalizing(UUID pathId, String statusText) {
        mutate(pathId, builder -> builder
                .phase("FINALIZING")
                .statusText(statusText)
                .progressPercent(90));
    }

    public GenerationProgressResponse complete(UUID pathId, String statusText) {
        mutate(pathId, builder -> builder
                .phase("COMPLETE")
                .statusText(statusText)
                .currentTopicName(null)
                .progressPercent(100)
                .complete(true)
                .failed(false)
                .errorMessage(null));
        return progressMap.get(pathId);
    }

    public GenerationProgressResponse fail(UUID pathId, String errorMessage) {
        mutate(pathId, builder -> builder
                .phase("FAILED")
                .statusText("Generation failed.")
                .currentTopicName(null)
                .failed(true)
                .complete(false)
                .errorMessage(errorMessage)
                .progressPercent(100));
        return progressMap.get(pathId);
    }

    public GenerationProgressResponse get(UUID pathId) {
        return progressMap.get(pathId);
    }

    private void mutate(UUID pathId, java.util.function.UnaryOperator<GenerationProgressResponse.GenerationProgressResponseBuilder> mutator) {
        progressMap.computeIfPresent(pathId, (id, current) -> mutator.apply(current.toBuilder()).build());
    }
}
