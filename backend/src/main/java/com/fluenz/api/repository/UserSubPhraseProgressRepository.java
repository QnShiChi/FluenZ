package com.fluenz.api.repository;

import com.fluenz.api.entity.UserSubPhraseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubPhraseProgressRepository extends JpaRepository<UserSubPhraseProgress, UUID> {

    Optional<UserSubPhraseProgress> findByUserIdAndSubPhraseId(UUID userId, UUID subPhraseId);

    List<UserSubPhraseProgress> findByUserId(UUID userId);
}
