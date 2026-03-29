package com.fluenz.api.repository;

import com.fluenz.api.entity.UserDefaultChunkProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.UUID;

@Repository
public interface UserDefaultChunkProgressRepository extends JpaRepository<UserDefaultChunkProgress, UUID> {
    Optional<UserDefaultChunkProgress> findByUserIdAndDefaultChunkId(UUID userId, UUID defaultChunkId);
    boolean existsByUserIdAndDefaultChunkId(UUID userId, UUID defaultChunkId);
    java.util.List<UserDefaultChunkProgress> findByUserId(UUID userId);
}
