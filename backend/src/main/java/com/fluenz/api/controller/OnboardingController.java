package com.fluenz.api.controller;

import com.fluenz.api.dto.request.OnboardingRequest;
import com.fluenz.api.dto.response.LearningPathResponse;
import com.fluenz.api.service.ImagePopulationService;
import com.fluenz.api.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final ImagePopulationService imagePopulationService;

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

    @PostMapping("/populate-images/{pathId}")
    public ResponseEntity<Map<String, String>> populateImages(@PathVariable UUID pathId) {
        imagePopulationService.populateImagesAsync(pathId);
        return ResponseEntity.ok(Map.of("status", "Image population triggered for path: " + pathId));
    }
}
