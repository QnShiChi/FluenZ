package com.fluenz.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgressDeltaResponse {
    private int gainedMinutes;
    private boolean didReachGoal;
    private int newStreak;
}
