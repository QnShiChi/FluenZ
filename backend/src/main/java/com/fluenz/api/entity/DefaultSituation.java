package com.fluenz.api.entity;

import com.fluenz.api.entity.enums.Level;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "default_situations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultSituation extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private DefaultTopic topic;

    @OneToMany(mappedBy = "situation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DefaultChunk> chunks = new ArrayList<>();

    @OneToMany(mappedBy = "defaultSituation", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<DefaultPracticeSession> practiceSessions = new ArrayList<>();
}
