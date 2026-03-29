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
public class ChunkResponse {
    private UUID id;
    private String contextQuestion;
    private String contextTranslation;
    private String rootSentence;
    private String rootTranslation;
    private String rootIpa;
    private int orderIndex;
    private boolean isCompleted;
    private List<SubPhraseResponse> subPhrases;
}
