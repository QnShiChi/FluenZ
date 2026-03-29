package com.fluenz.api.entity;

import com.fluenz.api.entity.enums.DefaultCatalogVersionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "default_catalog_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultCatalogVersion extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Integer versionNumber;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefaultCatalogVersionStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;

    @OneToMany(mappedBy = "catalogVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DefaultTopic> topics = new ArrayList<>();
}
