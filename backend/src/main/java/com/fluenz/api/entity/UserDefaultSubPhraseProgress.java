package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_default_sub_phrase_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "default_sub_phrase_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDefaultSubPhraseProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_sub_phrase_id", nullable = false)
    private DefaultSubPhrase defaultSubPhrase;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isLearned = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isBookmarked = false;
}
