package com.fluenz.api.repository;

import com.fluenz.api.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.UUID;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, UUID> {
    Optional<UserStats> findByUserId(UUID userId);
}
