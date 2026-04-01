package com.fluenz.api.dto.response;

import com.fluenz.api.entity.enums.LearningMode;
import com.fluenz.api.entity.enums.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathResponse {
    private UUID id;
    private String title;
    private String status;
    private LearningMode learningMode;
    private String professionName;
    private Level userLevel;
    private int topicCount;
    private int publishedTopicCount;
    private int generatedTopicCount;
    private int totalTopicCount;
    private String generationPhase;
    private boolean generationInProgress;
    private int situationCount;
    private int chunkCount;
    private List<TopicResponse> topics;
}
