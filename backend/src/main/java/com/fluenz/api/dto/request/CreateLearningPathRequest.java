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
public class CreateLearningPathRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Profession ID is required")
    private UUID professionId;
}
