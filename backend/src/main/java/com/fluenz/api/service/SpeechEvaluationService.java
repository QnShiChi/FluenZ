package com.fluenz.api.service;

import com.fluenz.api.dto.response.EvaluationResponse;
import com.fluenz.api.dto.response.EvaluationResponse.WordDetail;
import com.fluenz.api.dto.response.EvaluationResponse.WordStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpeechEvaluationService {

    public EvaluationResponse evaluate(String expectedText, String actualText) {
        String[] expected = tokenize(expectedText);
        String[] actual = tokenize(actualText);

        // Build DP table for word-level Levenshtein
        int m = expected.length;
        int n = actual.length;
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (expected[i - 1].equals(actual[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j - 1], // substitution
                            Math.min(dp[i - 1][j], dp[i][j - 1]) // deletion, insertion
                    );
                }
            }
        }

        // Backtrack to find word-level alignment
        List<WordDetail> wordDetails = new ArrayList<>();
        int i = m, j = n;
        List<WordDetail> reversed = new ArrayList<>();

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && expected[i - 1].equals(actual[j - 1])) {
                reversed.add(new WordDetail(expected[i - 1], WordStatus.CORRECT));
                i--;
                j--;
            } else if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + 1) {
                // substitution — word is wrong
                reversed.add(new WordDetail(expected[i - 1], WordStatus.WRONG));
                i--;
                j--;
            } else if (i > 0 && dp[i][j] == dp[i - 1][j] + 1) {
                // deletion — word is missing from actual
                reversed.add(new WordDetail(expected[i - 1], WordStatus.MISSING));
                i--;
            } else {
                // insertion — extra word in actual, skip
                j--;
            }
        }

        // Reverse to get correct order
        for (int k = reversed.size() - 1; k >= 0; k--) {
            wordDetails.add(reversed.get(k));
        }

        // Calculate score
        int errors = (int) wordDetails.stream()
                .filter(w -> w.getStatus() != WordStatus.CORRECT)
                .count();
        int totalWords = expected.length;
        int overallScore = totalWords == 0 ? 100
                : Math.max(0, (int) Math.round(100.0 - ((double) errors / totalWords * 100.0)));

        return new EvaluationResponse(overallScore, wordDetails);
    }

    private String[] tokenize(String text) {
        if (text == null || text.isBlank()) return new String[0];
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s']", "")
                .trim()
                .split("\\s+");
    }
}
