package com.fluenz.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubPhraseResponse {
    private UUID id;
    private String text;
    private String translation;
    private String ipa;
    private List<String> distractors;
    private int orderIndex;
    private boolean isLearned;
    private boolean isBookmarked;
}
