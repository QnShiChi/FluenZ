package com.fluenz.api.entity;

import com.fluenz.api.entity.enums.PathStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_paths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PathStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profession_id")
    private Profession profession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_profile_id")
    private LearnerProfile learnerProfile;

    @Column(length = 64)
    private String generationPhase;

    @Builder.Default
    @Column(name = "published_topic_count")
    private Integer publishedTopicCount = 0;

    @Builder.Default
    @Column(name = "generated_topic_count")
    private Integer generatedTopicCount = 0;

    @Builder.Default
    @Column(name = "total_topic_count")
    private Integer totalTopicCount = 0;

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Topic> topics = new ArrayList<>();
}
