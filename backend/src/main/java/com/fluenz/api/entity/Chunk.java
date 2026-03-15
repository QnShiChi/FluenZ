package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chunk extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String phrase;

    @Column(columnDefinition = "TEXT")
    private String translation;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_id", nullable = false)
    private Situation situation;
}
