package com.fluenz.api.dto.response;

import com.fluenz.api.entity.enums.Level;
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
public class SituationResponse {
    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private Level level;
    private int orderIndex;
    private int chunkCount;
    private List<ChunkResponse> chunks;
}
