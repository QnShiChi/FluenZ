package com.fluenz.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UserProfileResponse {
    private String username;
    private String email;
    private String currentLevel;
    
    private int totalLearningMinutes;
    private int currentStreak;
    private int longestStreak;
    private int totalSpokenCount;
    
    private int todayMinutes;
    @JsonProperty("isTodayGoalReached")
    private boolean isTodayGoalReached;
    
    private List<DailyActivityDto> weeklyActivities;

    @Data
    @Builder
    public static class DailyActivityDto {
        private LocalDate date;
        private int minutes;
        private boolean goalReached;
    }
}
