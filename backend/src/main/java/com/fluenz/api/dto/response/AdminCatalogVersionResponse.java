package com.fluenz.api.dto.response;

import com.fluenz.api.entity.enums.DefaultCatalogVersionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCatalogVersionResponse {
    private UUID id;
    private Integer versionNumber;
    private String title;
    private DefaultCatalogVersionStatus status;
    private boolean published;
    private int topicCount;
}
