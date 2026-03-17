package com.fluenz.api.controller;

import com.fluenz.api.entity.SubPhrase;
import com.fluenz.api.entity.User;
import com.fluenz.api.entity.UserSubPhraseProgress;
import com.fluenz.api.repository.SubPhraseRepository;
import com.fluenz.api.repository.UserRepository;
import com.fluenz.api.repository.UserSubPhraseProgressRepository;
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

    @PutMapping("/{subPhraseId}/learned")
    public ResponseEntity<Map<String, Object>> toggleLearned(
            @PathVariable UUID subPhraseId,
            Authentication authentication
    ) {
        UserSubPhraseProgress progress = getOrCreateProgress(authentication.getName(), subPhraseId);
        progress.setIsLearned(!progress.getIsLearned());
        progressRepository.save(progress);
        return ResponseEntity.ok(Map.of(
                "subPhraseId", subPhraseId,
                "isLearned", progress.getIsLearned()
        ));
    }

    @PutMapping("/{subPhraseId}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable UUID subPhraseId,
            Authentication authentication
    ) {
        UserSubPhraseProgress progress = getOrCreateProgress(authentication.getName(), subPhraseId);
        progress.setIsBookmarked(!progress.getIsBookmarked());
        progressRepository.save(progress);
        return ResponseEntity.ok(Map.of(
                "subPhraseId", subPhraseId,
                "isBookmarked", progress.getIsBookmarked()
        ));
    }

    private UserSubPhraseProgress getOrCreateProgress(String email, UUID subPhraseId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        SubPhrase subPhrase = subPhraseRepository.findById(subPhraseId)
                .orElseThrow(() -> new RuntimeException("SubPhrase not found"));

        return progressRepository.findByUserIdAndSubPhraseId(user.getId(), subPhraseId)
                .orElseGet(() -> UserSubPhraseProgress.builder()
                        .user(user)
                        .subPhrase(subPhrase)
                        .isLearned(false)
                        .isBookmarked(false)
                        .build());
    }
}
