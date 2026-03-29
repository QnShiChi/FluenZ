package com.fluenz.api.repository;

import com.fluenz.api.entity.UserDefaultSubPhraseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDefaultSubPhraseProgressRepository extends JpaRepository<UserDefaultSubPhraseProgress, UUID> {

    Optional<UserDefaultSubPhraseProgress> findByUserIdAndDefaultSubPhraseId(UUID userId, UUID defaultSubPhraseId);

    List<UserDefaultSubPhraseProgress> findByUserId(UUID userId);
}
