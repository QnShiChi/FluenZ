package com.fluenz.api.dto.response;

import com.fluenz.api.entity.enums.LearningMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningModeResponse {
    private LearningMode preferredLearningMode;
    private boolean onboardingRequired;
}
