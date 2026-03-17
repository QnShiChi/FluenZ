package com.fluenz.api.controller;

import com.fluenz.api.dto.response.LearningPathResponse;
import com.fluenz.api.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final OnboardingService onboardingService;

    @GetMapping("/active")
    public ResponseEntity<LearningPathResponse> getActivePath(Authentication authentication) {
        LearningPathResponse response = onboardingService.getActivePath(authentication.getName());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}
