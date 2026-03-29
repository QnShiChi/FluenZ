package com.fluenz.api.repository;

import com.fluenz.api.entity.UserChunkProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.UUID;

@Repository
public interface UserChunkProgressRepository extends JpaRepository<UserChunkProgress, UUID> {
    Optional<UserChunkProgress> findByUserIdAndChunkId(UUID userId, UUID chunkId);
    boolean existsByUserIdAndChunkId(UUID userId, UUID chunkId);
}
