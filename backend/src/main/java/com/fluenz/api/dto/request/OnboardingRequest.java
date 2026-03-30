package com.fluenz.api.dto.request;

import com.fluenz.api.entity.enums.Level;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
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
public class OnboardingRequest {

    private UUID professionId;

    @NotNull(message = "Level is required")
    private Level level;

    private String jobRole;
    private String industry;
    private String seniority;
    private List<String> communicateWith;
    private List<String> communicationChannels;
    private List<String> communicationContexts;
    private List<String> painPoints;
    private List<String> goals;
    private String customGoal;
    private String customContext;
    private String personaSummary;

    private String specificGoals;

    @AssertTrue(message = "Either professionId or jobRole is required")
    public boolean hasLearningPersonaTarget() {
        return professionId != null || (jobRole != null && !jobRole.isBlank());
    }
}
