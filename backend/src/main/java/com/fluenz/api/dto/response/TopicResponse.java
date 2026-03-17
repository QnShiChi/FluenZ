package com.fluenz.api.dto.response;

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
public class TopicResponse {
    private UUID id;
    private String name;
    private int orderIndex;
    private int situationCount;
    private List<SituationResponse> situations;
}
