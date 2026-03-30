package com.fluenz.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaPreviewResponse {
    private String personaSummary;
    private List<String> communicationPriorities;
    private boolean isAiGenerated;
}
