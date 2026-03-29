package com.fluenz.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDefaultChunkRequest {

    @NotBlank(message = "Context question is required")
    private String contextQuestion;

    private String contextTranslation;

    @NotBlank(message = "Root sentence is required")
    private String rootSentence;

    private String rootTranslation;

    private String rootIpa;
}
