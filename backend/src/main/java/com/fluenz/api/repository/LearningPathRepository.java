package com.fluenz.api.repository;

import com.fluenz.api.entity.LearningPath;
import com.fluenz.api.entity.User;
import com.fluenz.api.entity.enums.PathStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, UUID> {

    List<LearningPath> findByUserAndStatus(User user, PathStatus status);

    List<LearningPath> findByUserId(UUID userId);

    Optional<LearningPath> findFirstByUserAndStatusOrderByCreatedAtDesc(User user, PathStatus status);

    @EntityGraph(attributePaths = {"user", "profession", "learnerProfile"})
    Optional<LearningPath> findWithGenerationContextById(UUID id);
}
