package com.fluenz.api.entity;

import com.fluenz.api.entity.enums.LearningMode;
import com.fluenz.api.entity.enums.Level;
import com.fluenz.api.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level currentLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LearningMode preferredLearningMode = LearningMode.DEFAULT;

    @Column(columnDefinition = "TEXT")
    private String goals;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_default_catalog_version_id")
    private DefaultCatalogVersion activeDefaultCatalogVersion;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LearningPath> learningPaths = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PracticeLog> practiceLogs = new ArrayList<>();
}
