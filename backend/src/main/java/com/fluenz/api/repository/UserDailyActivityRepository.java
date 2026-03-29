package com.fluenz.api.repository;

import com.fluenz.api.entity.UserDailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

@Repository
public interface UserDailyActivityRepository extends JpaRepository<UserDailyActivity, UUID> {
    Optional<UserDailyActivity> findByUserIdAndDate(UUID userId, LocalDate date);
    List<UserDailyActivity> findByUserIdAndDateBetweenOrderByDateAsc(UUID userId, LocalDate startDate, LocalDate endDate);
}
