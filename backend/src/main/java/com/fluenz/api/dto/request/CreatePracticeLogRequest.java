package com.fluenz.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePracticeLogRequest {

    @NotNull(message = "Chunk ID is required")
    private UUID chunkId;

    private Double pronunciationScore;

    private String userAudioUrl;
}
