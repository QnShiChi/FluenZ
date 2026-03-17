package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chunk extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contextQuestion;

    @Column(columnDefinition = "TEXT")
    private String contextTranslation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rootSentence;

    @Column(columnDefinition = "TEXT")
    private String rootTranslation;

    @Column(columnDefinition = "TEXT")
    private String rootIpa;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_id", nullable = false)
    private Situation situation;

    @OneToMany(mappedBy = "chunk", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubPhrase> subPhrases = new ArrayList<>();
}
