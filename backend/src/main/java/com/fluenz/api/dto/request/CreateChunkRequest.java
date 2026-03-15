package com.fluenz.api.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class CreateChunkRequest {

    @NotBlank(message = "Phrase is required")
    private String phrase;

    private String translation;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    @NotNull(message = "Situation ID is required")
    private UUID situationId;
}
