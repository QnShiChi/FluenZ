package com.fluenz.api.dto.response;

import com.fluenz.api.entity.enums.PathStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathResponse {
    private UUID id;
    private String title;
    private PathStatus status;
    private UUID professionId;
    private String professionName;
    private LocalDateTime createdAt;
}
