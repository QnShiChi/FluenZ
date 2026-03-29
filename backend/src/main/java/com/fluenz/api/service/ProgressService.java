package com.fluenz.api.service;

import com.fluenz.api.dto.response.ProgressDeltaResponse;
import com.fluenz.api.dto.response.UserProfileResponse;
import com.fluenz.api.entity.*;
import com.fluenz.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final UserDailyActivityRepository dailyActivityRepository;
    private final UserChunkProgressRepository chunkProgressRepository;
    private final UserDefaultChunkProgressRepository defaultChunkProgressRepository;
    private final UserSubPhraseProgressRepository subPhraseProgressRepository;
    private final UserDefaultSubPhraseProgressRepository defaultSubPhraseProgressRepository;
    private final ChunkRepository chunkRepository;
    private final DefaultChunkRepository defaultChunkRepository;

    private static final int MAX_SECONDS_PER_CHUNK = 10 * 60;
    private static final int DAILY_GOAL_MINUTES = 5;

    @Transactional
    public ProgressDeltaResponse markChunkComplete(String email, UUID chunkId, boolean isDefault, int totalTimeSeconds) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (isDefault) {
            DefaultChunk chunk = defaultChunkRepository.findById(chunkId)
                    .orElseThrow(() -> new RuntimeException("Default Chunk not found"));
            UserDefaultChunkProgress progress = defaultChunkProgressRepository
                    .findByUserIdAndDefaultChunkId(user.getId(), chunkId)
                    .orElse(null);

            if (progress == null) {
                progress = UserDefaultChunkProgress.builder()
                        .user(user).defaultChunk(chunk).isCompleted(true)
                        .completedAt(LocalDateTime.now()).build();
                defaultChunkProgressRepository.save(progress);
            } else if (!Boolean.TRUE.equals(progress.getIsCompleted())) {
                progress.setIsCompleted(true);
                progress.setCompletedAt(LocalDateTime.now());
                defaultChunkProgressRepository.save(progress);
            }

            markDefaultSubPhrasesLearned(user, chunk);
        } else {
            Chunk chunk = chunkRepository.findById(chunkId)
                    .orElseThrow(() -> new RuntimeException("Chunk not found"));
            UserChunkProgress progress = chunkProgressRepository
                    .findByUserIdAndChunkId(user.getId(), chunkId)
                    .orElse(null);

            if (progress == null) {
                progress = UserChunkProgress.builder()
                        .user(user).chunk(chunk).isCompleted(true)
                        .completedAt(LocalDateTime.now()).build();
                chunkProgressRepository.save(progress);
            } else if (!Boolean.TRUE.equals(progress.getIsCompleted())) {
                progress.setIsCompleted(true);
                progress.setCompletedAt(LocalDateTime.now());
                chunkProgressRepository.save(progress);
            }

            markSubPhrasesLearned(user, chunk);
        }

        int gainedMinutes = calculateGainedMinutes(totalTimeSeconds);

        return updateDailyProgressAndStats(user, gainedMinutes);
    }

    static int calculateGainedMinutes(int totalTimeSeconds) {
        if (totalTimeSeconds <= 0) {
            return 0;
        }

        int cappedSeconds = Math.min(totalTimeSeconds, MAX_SECONDS_PER_CHUNK);
        return (int) Math.ceil(cappedSeconds / 60.0);
    }

    private void markSubPhrasesLearned(User user, Chunk chunk) {
        for (SubPhrase subPhrase : chunk.getSubPhrases()) {
            UserSubPhraseProgress progress = subPhraseProgressRepository
                    .findByUserIdAndSubPhraseId(user.getId(), subPhrase.getId())
                    .orElseGet(() -> UserSubPhraseProgress.builder()
                            .user(user)
                            .subPhrase(subPhrase)
                            .isLearned(false)
                            .isBookmarked(false)
                            .build());

            if (!Boolean.TRUE.equals(progress.getIsLearned())) {
                progress.setIsLearned(true);
            }

            subPhraseProgressRepository.save(progress);
        }
    }

    private void markDefaultSubPhrasesLearned(User user, DefaultChunk chunk) {
        for (DefaultSubPhrase subPhrase : chunk.getSubPhrases()) {
            UserDefaultSubPhraseProgress progress = defaultSubPhraseProgressRepository
                    .findByUserIdAndDefaultSubPhraseId(user.getId(), subPhrase.getId())
                    .orElseGet(() -> UserDefaultSubPhraseProgress.builder()
                            .user(user)
                            .defaultSubPhrase(subPhrase)
                            .isLearned(false)
                            .isBookmarked(false)
                            .build());

            if (!Boolean.TRUE.equals(progress.getIsLearned())) {
                progress.setIsLearned(true);
            }

            defaultSubPhraseProgressRepository.save(progress);
        }
    }

    private ProgressDeltaResponse updateDailyProgressAndStats(User user, int gainedMinutes) {
        LocalDate today = LocalDate.now();

        // 1. Update or Create Daily Activity
        UserDailyActivity dailyActivity = dailyActivityRepository.findByUserIdAndDate(user.getId(), today)
                .orElse(null);

        if (dailyActivity == null) {
            dailyActivity = UserDailyActivity.builder()
                    .user(user).date(today).learningMinutes(0).isGoalReached(false).build();
            dailyActivity = dailyActivityRepository.save(dailyActivity);
        }

        boolean wasGoalReached = Boolean.TRUE.equals(dailyActivity.getIsGoalReached());
        dailyActivity.setLearningMinutes(dailyActivity.getLearningMinutes() + gainedMinutes);
        
        boolean isGoalReachedNow = dailyActivity.getLearningMinutes() >= DAILY_GOAL_MINUTES;
        dailyActivity.setIsGoalReached(isGoalReachedNow);
        
        dailyActivityRepository.save(dailyActivity);

        // 2. Update or Create Stats (Streak, Total Minutes)
        UserStats stats = getOrCreateStats(user);

        stats.setTotalLearningMinutes(stats.getTotalLearningMinutes() + gainedMinutes);

        // Streak logic: only fires if goal was just reached this session
        if (!wasGoalReached && isGoalReachedNow) {
            LocalDate lastLearningDate = stats.getLastLearningDate();
            
            if (lastLearningDate == null || ChronoUnit.DAYS.between(lastLearningDate, today) > 1) {
                stats.setCurrentStreak(1);
            } else if (ChronoUnit.DAYS.between(lastLearningDate, today) == 1) {
                stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            }

            if (stats.getCurrentStreak() > stats.getLongestStreak()) {
                stats.setLongestStreak(stats.getCurrentStreak());
            }

            stats.setLastLearningDate(today);
        }
        
        userStatsRepository.save(stats);

        return ProgressDeltaResponse.builder()
                .gainedMinutes(gainedMinutes)
                .didReachGoal(isGoalReachedNow)
                .newStreak(stats.getCurrentStreak())
                .build();
    }

    @Transactional
    public void recordVoiceActivity(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserStats stats = getOrCreateStats(user);
        stats.setTotalSpokenCount(stats.getTotalSpokenCount() + 1);
        userStatsRepository.save(stats);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserStats stats = userStatsRepository.findByUserId(user.getId()).orElse(null);

        LocalDate today = LocalDate.now();
        UserDailyActivity dailyActivity = dailyActivityRepository.findByUserIdAndDate(user.getId(), today)
                .orElse(null);

        // Get past 7 days calendar
        LocalDate startDate = today.minusDays(6);
        List<UserDailyActivity> recentActivities = dailyActivityRepository
                .findByUserIdAndDateBetweenOrderByDateAsc(user.getId(), startDate, today);

        List<UserProfileResponse.DailyActivityDto> weeklyActivities = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = startDate.plusDays(i);
            UserDailyActivity act = recentActivities.stream()
                    .filter(a -> a.getDate().equals(d))
                    .findFirst()
                    .orElse(null);
            
            weeklyActivities.add(UserProfileResponse.DailyActivityDto.builder()
                    .date(d)
                    .minutes(act != null ? act.getLearningMinutes() : 0)
                    .goalReached(act != null && Boolean.TRUE.equals(act.getIsGoalReached()))
                    .build());
        }

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .currentLevel(user.getCurrentLevel().name())
                .totalLearningMinutes(stats != null ? stats.getTotalLearningMinutes() : 0)
                .currentStreak(stats != null ? stats.getCurrentStreak() : 0)
                .longestStreak(stats != null ? stats.getLongestStreak() : 0)
                .totalSpokenCount(stats != null ? stats.getTotalSpokenCount() : 0)
                .todayMinutes(dailyActivity != null ? dailyActivity.getLearningMinutes() : 0)
                .isTodayGoalReached(dailyActivity != null && Boolean.TRUE.equals(dailyActivity.getIsGoalReached()))
                .weeklyActivities(weeklyActivities)
                .build();
    }

    /**
     * Get existing UserStats or create + save a new one immediately.
     * This avoids the duplicate-key issue from using orElseGet with an unsaved entity.
     */
    private UserStats getOrCreateStats(User user) {
        return userStatsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserStats newStats = UserStats.builder()
                            .user(user)
                            .totalLearningMinutes(0)
                            .currentStreak(0)
                            .longestStreak(0)
                            .totalSpokenCount(0)
                            .build();
                    return userStatsRepository.save(newStats);
                });
    }
}
