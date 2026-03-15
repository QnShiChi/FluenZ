package com.fluenz.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeLogResponse {
    private UUID id;
    private UUID chunkId;
    private Double pronunciationScore;
    private LocalDateTime practicedAt;
}
