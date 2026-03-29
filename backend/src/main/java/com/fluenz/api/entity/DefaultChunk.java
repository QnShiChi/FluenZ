package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "default_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultChunk extends BaseEntity {

    @Column(nullable = false)
    private String contextQuestion;

    @Column(columnDefinition = "TEXT")
    private String contextTranslation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rootSentence;

    @Column(columnDefinition = "TEXT")
    private String rootTranslation;

    @Column
    private String rootIpa;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_id", nullable = false)
    private DefaultSituation situation;

    @OneToMany(mappedBy = "chunk", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DefaultSubPhrase> subPhrases = new ArrayList<>();
}
