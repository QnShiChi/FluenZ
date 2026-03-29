package com.fluenz.api.controller;

import com.fluenz.api.dto.request.ChunkCompleteRequest;
import com.fluenz.api.entity.DefaultSubPhrase;
import com.fluenz.api.entity.SubPhrase;
import com.fluenz.api.entity.User;
import com.fluenz.api.entity.UserDefaultSubPhraseProgress;
import com.fluenz.api.entity.UserSubPhraseProgress;
import com.fluenz.api.repository.DefaultSubPhraseRepository;
import com.fluenz.api.repository.UserDefaultSubPhraseProgressRepository;
import com.fluenz.api.repository.SubPhraseRepository;
import com.fluenz.api.repository.UserRepository;
import com.fluenz.api.repository.UserSubPhraseProgressRepository;
import com.fluenz.api.service.ProgressService;
import com.fluenz.api.dto.response.ProgressDeltaResponse;
import com.fluenz.api.dto.response.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final UserRepository userRepository;
    private final SubPhraseRepository subPhraseRepository;
    private final UserSubPhraseProgressRepository progressRepository;
    private final DefaultSubPhraseRepository defaultSubPhraseRepository;
    private final UserDefaultSubPhraseProgressRepository defaultProgressRepository;
    private final ProgressService progressService;

    @PostMapping("/chunk-complete/{chunkId}")
    public ResponseEntity<ProgressDeltaResponse> completeChunk(
            @PathVariable UUID chunkId,
            @RequestParam(defaultValue = "false") boolean isDefault,
            @RequestBody(required = false) ChunkCompleteRequest request,
            Authentication authentication
    ) {
        int totalTimeSeconds = request != null ? request.getTotalTimeSeconds() : 0;
        return ResponseEntity.ok(progressService.markChunkComplete(authentication.getName(), chunkId, isDefault, totalTimeSeconds));
    }

    @PostMapping("/voice-activity")
    public ResponseEntity<Void> recordVoiceActivity(Authentication authentication) {
        progressService.recordVoiceActivity(authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(progressService.getUserProfile(authentication.getName()));
    }

    @PutMapping("/{subPhraseId}/learned")
    public ResponseEntity<Map<String, Object>> toggleLearned(
            @PathVariable UUID subPhraseId,
            Authentication authentication
    ) {
        ProgressToggleResult result = getOrCreateProgress(authentication.getName(), subPhraseId);
        result.toggleLearned();
        return ResponseEntity.ok(Map.of(
                "subPhraseId", subPhraseId,
                "isLearned", result.isLearned()
        ));
    }

    @PutMapping("/{subPhraseId}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable UUID subPhraseId,
            Authentication authentication
    ) {
        ProgressToggleResult result = getOrCreateProgress(authentication.getName(), subPhraseId);
        result.toggleBookmarked();
        return ResponseEntity.ok(Map.of(
                "subPhraseId", subPhraseId,
                "isBookmarked", result.isBookmarked()
        ));
    }

    private ProgressToggleResult getOrCreateProgress(String email, UUID subPhraseId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return subPhraseRepository.findById(subPhraseId)
                .<ProgressToggleResult>map(subPhrase -> new PersonalizedProgressToggleResult(
                        progressRepository.findByUserIdAndSubPhraseId(user.getId(), subPhraseId)
                                .orElseGet(() -> UserSubPhraseProgress.builder()
                                        .user(user)
                                        .subPhrase(subPhrase)
                                        .isLearned(false)
                                        .isBookmarked(false)
                                        .build())
                ))
                .or(() -> defaultSubPhraseRepository.findById(subPhraseId)
                        .map(defaultSubPhrase -> new DefaultProgressToggleResult(
                                defaultProgressRepository.findByUserIdAndDefaultSubPhraseId(user.getId(), subPhraseId)
                                        .orElseGet(() -> UserDefaultSubPhraseProgress.builder()
                                                .user(user)
                                                .defaultSubPhrase(defaultSubPhrase)
                                                .isLearned(false)
                                                .isBookmarked(false)
                                                .build())
                        )))
                .orElseThrow(() -> new RuntimeException("SubPhrase not found"));
    }

    private sealed interface ProgressToggleResult permits PersonalizedProgressToggleResult, DefaultProgressToggleResult {
        void toggleLearned();
        void toggleBookmarked();
        boolean isLearned();
        boolean isBookmarked();
    }

    private final class PersonalizedProgressToggleResult implements ProgressToggleResult {
        private final UserSubPhraseProgress progress;

        private PersonalizedProgressToggleResult(UserSubPhraseProgress progress) {
            this.progress = progress;
        }

        @Override
        public void toggleLearned() {
            progress.setIsLearned(!progress.getIsLearned());
            progressRepository.save(progress);
        }

        @Override
        public void toggleBookmarked() {
            progress.setIsBookmarked(!progress.getIsBookmarked());
            progressRepository.save(progress);
        }

        @Override
        public boolean isLearned() {
            return Boolean.TRUE.equals(progress.getIsLearned());
        }

        @Override
        public boolean isBookmarked() {
            return Boolean.TRUE.equals(progress.getIsBookmarked());
        }
    }

    private final class DefaultProgressToggleResult implements ProgressToggleResult {
        private final UserDefaultSubPhraseProgress progress;

        private DefaultProgressToggleResult(UserDefaultSubPhraseProgress progress) {
            this.progress = progress;
        }

        @Override
        public void toggleLearned() {
            progress.setIsLearned(!progress.getIsLearned());
            defaultProgressRepository.save(progress);
        }

        @Override
        public void toggleBookmarked() {
            progress.setIsBookmarked(!progress.getIsBookmarked());
            defaultProgressRepository.save(progress);
        }

        @Override
        public boolean isLearned() {
            return Boolean.TRUE.equals(progress.getIsLearned());
        }

        @Override
        public boolean isBookmarked() {
            return Boolean.TRUE.equals(progress.getIsBookmarked());
        }
    }
}
