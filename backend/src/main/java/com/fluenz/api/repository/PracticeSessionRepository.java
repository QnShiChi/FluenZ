package com.fluenz.api.repository;

import com.fluenz.api.entity.PracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, UUID> {
    List<PracticeSession> findByUserIdOrderByCompletedAtDesc(UUID userId);
    List<PracticeSession> findByUserIdAndSituationId(UUID userId, UUID situationId);
}
