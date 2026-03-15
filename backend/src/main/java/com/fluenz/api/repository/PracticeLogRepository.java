package com.fluenz.api.repository;

import com.fluenz.api.entity.PracticeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PracticeLogRepository extends JpaRepository<PracticeLog, UUID> {

    List<PracticeLog> findByUserIdAndChunkId(UUID userId, UUID chunkId);

    List<PracticeLog> findByUserIdAndChunkSituationId(UUID userId, UUID situationId);
}
