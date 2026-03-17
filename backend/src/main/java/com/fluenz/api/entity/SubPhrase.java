package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sub_phrases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubPhrase extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "TEXT")
    private String translation;

    @Column(columnDefinition = "TEXT")
    private String ipa;

    @Column(columnDefinition = "TEXT")
    private String distractors;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id", nullable = false)
    private Chunk chunk;
}
