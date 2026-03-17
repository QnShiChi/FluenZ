package com.fluenz.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeCompleteRequest {
    private UUID situationId;
    private int totalTimeSeconds;
    private int overallScore;
    private List<String> failedWords;
}
