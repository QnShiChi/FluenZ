package com.fluenz.api.controller;

import com.fluenz.api.dto.request.OnboardingRequest;
import com.fluenz.api.dto.response.GenerationProgressResponse;
import com.fluenz.api.dto.response.LearningPathResponse;
import com.fluenz.api.dto.response.PersonaPreviewResponse;
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
    public ResponseEntity<GenerationProgressResponse> generate(
            @Valid @RequestBody OnboardingRequest request,
            Authentication authentication
    ) {
        GenerationProgressResponse response = onboardingService.startGeneration(
                authentication.getName(), request
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/persona-preview")
    public ResponseEntity<PersonaPreviewResponse> previewPersona(
            @Valid @RequestBody OnboardingRequest request
    ) {
        return ResponseEntity.ok(onboardingService.previewPersona(request));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> hasPath(Authentication authentication) {
        boolean hasPath = onboardingService.hasActivePath(authentication.getName());
        java.util.UUID generatingPathId = onboardingService.getLatestGeneratingPathId(authentication.getName()).orElse(null);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("hasActivePath", hasPath);
        response.put("generationInProgress", generatingPathId != null);
        response.put("generationPathId", generatingPathId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generation-progress/{pathId}")
    public ResponseEntity<GenerationProgressResponse> getGenerationProgress(
            @PathVariable UUID pathId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(onboardingService.getGenerationProgress(authentication.getName(), pathId));
    }

    @PostMapping("/populate-images/{pathId}")
    public ResponseEntity<Map<String, String>> populateImages(@PathVariable UUID pathId) {
        imagePopulationService.populateImagesAsync(pathId);
        return ResponseEntity.ok(Map.of("status", "Image population triggered for path: " + pathId));
    }
}
