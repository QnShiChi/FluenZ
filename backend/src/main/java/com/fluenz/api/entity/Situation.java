package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "situations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Situation extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @OneToMany(mappedBy = "situation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Chunk> chunks = new ArrayList<>();
}
