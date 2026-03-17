package com.fluenz.api.dto.response;

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
    private String professionName;
    private Level userLevel;
    private int topicCount;
    private int situationCount;
    private int chunkCount;
    private List<TopicResponse> topics;
}
