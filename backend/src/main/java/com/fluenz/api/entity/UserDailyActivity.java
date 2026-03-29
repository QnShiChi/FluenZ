package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_daily_activities",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "activity_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDailyActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Builder.Default
    private Integer learningMinutes = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isGoalReached = false;
}
