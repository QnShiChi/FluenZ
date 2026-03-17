package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_sub_phrase_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sub_phrase_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubPhraseProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_phrase_id", nullable = false)
    private SubPhrase subPhrase;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isLearned = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isBookmarked = false;
}
