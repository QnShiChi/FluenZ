package com.fluenz.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateTextRequest {
    @NotBlank(message = "Expected text is required")
    private String expectedText;

    @NotBlank(message = "Actual text is required")
    private String actualText;
}
