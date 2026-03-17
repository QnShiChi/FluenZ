package com.fluenz.api.controller;

import com.fluenz.api.dto.request.OnboardingRequest;
import com.fluenz.api.dto.response.LearningPathResponse;
import com.fluenz.api.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/generate")
    public ResponseEntity<LearningPathResponse> generate(
            @Valid @RequestBody OnboardingRequest request,
            Authentication authentication
    ) {
        LearningPathResponse response = onboardingService.generatePath(
                authentication.getName(), request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> hasPath(Authentication authentication) {
        boolean hasPath = onboardingService.hasActivePath(authentication.getName());
        return ResponseEntity.ok(Map.of("hasActivePath", hasPath));
    }
}
