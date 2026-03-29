package com.fluenz.api.controller;

import com.fluenz.api.dto.request.UpdateLearningModeRequest;
import com.fluenz.api.dto.response.LearningModeResponse;
import com.fluenz.api.dto.response.LearningPathResponse;
import com.fluenz.api.service.LearningExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningExperienceService learningExperienceService;

    @GetMapping("/active")
    public ResponseEntity<LearningPathResponse> getActivePath(Authentication authentication) {
        LearningPathResponse response = learningExperienceService.getActivePath(authentication.getName());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/mode")
    public ResponseEntity<LearningModeResponse> updateLearningMode(
            @Valid @RequestBody UpdateLearningModeRequest request,
            Authentication authentication
    ) {
        LearningModeResponse response = learningExperienceService.updateLearningMode(
                authentication.getName(),
                request.getLearningMode()
        );
        return ResponseEntity.ok(response);
    }
}
