package com.fluenz.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDefaultSubPhraseRequest {

    @NotBlank(message = "Sub phrase text is required")
    private String text;

    private String translation;

    private String ipa;

    @Builder.Default
    private List<String> distractors = List.of();

    private String imageUrl;
}
