package com.fluenz.api.dto.request;

import com.fluenz.api.entity.enums.LearningMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLearningModeRequest {

    @NotNull(message = "Learning mode is required")
    private LearningMode learningMode;
}
