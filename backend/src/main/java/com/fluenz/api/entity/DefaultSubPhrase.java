package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "default_sub_phrases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultSubPhrase extends BaseEntity {

    @Column(nullable = false)
    private String text;

    @Column(columnDefinition = "TEXT")
    private String translation;

    @Column
    private String ipa;

    @Column(columnDefinition = "TEXT")
    private String distractors;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id", nullable = false)
    private DefaultChunk chunk;

    @OneToMany(mappedBy = "defaultSubPhrase", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<UserDefaultSubPhraseProgress> userProgressList = new ArrayList<>();
}
