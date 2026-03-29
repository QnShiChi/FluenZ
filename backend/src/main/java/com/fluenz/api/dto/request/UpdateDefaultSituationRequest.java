package com.fluenz.api.dto.request;

import com.fluenz.api.entity.enums.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDefaultSituationRequest {

    @NotBlank(message = "Situation title is required")
    private String title;

    private String description;

    private String thumbnailUrl;

    @NotNull(message = "Situation level is required")
    private Level level;
}
