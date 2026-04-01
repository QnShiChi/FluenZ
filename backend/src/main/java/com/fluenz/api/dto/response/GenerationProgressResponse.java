package com.fluenz.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GenerationProgressResponse {
    private UUID pathId;
    private String phase;
    private String statusText;
    private int currentBatch;
    private int totalBatches;
    private int completedTopics;
    private int publishedTopics;
    private int totalTopics;
    private String currentTopicName;
    private int progressPercent;
    private boolean textReady;
    private boolean assetsPending;
    private boolean complete;
    private boolean failed;
    private String errorMessage;
}
