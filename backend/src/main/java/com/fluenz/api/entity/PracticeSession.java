package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "practice_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_id", nullable = false)
    private Situation situation;

    @Column(nullable = false)
    private Integer totalTimeSeconds;

    @Column(nullable = false)
    private Integer overallScore;

    @Column(columnDefinition = "TEXT")
    private String failedWords;

    @Column(nullable = false)
    private LocalDateTime completedAt;
}
