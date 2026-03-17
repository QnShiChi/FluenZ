package com.fluenz.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    private int overallScore;
    private List<WordDetail> wordDetails;

    public enum WordStatus {
        CORRECT, WRONG, MISSING
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordDetail {
        private String word;
        private WordStatus status;
    }
}
