package com.fluenz.api.controller;

import com.fluenz.api.dto.response.ProfessionResponse;
import com.fluenz.api.repository.ProfessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/professions")
@RequiredArgsConstructor
public class ProfessionController {

    private final ProfessionRepository professionRepository;

    @GetMapping
    public ResponseEntity<List<ProfessionResponse>> getAll() {
        List<ProfessionResponse> professions = professionRepository.findAll().stream()
                .map(p -> ProfessionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .toList();
        return ResponseEntity.ok(professions);
    }
}
