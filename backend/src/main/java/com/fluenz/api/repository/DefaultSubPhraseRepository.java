package com.fluenz.api.repository;

import com.fluenz.api.entity.DefaultSubPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DefaultSubPhraseRepository extends JpaRepository<DefaultSubPhrase, UUID> {

    List<DefaultSubPhrase> findByChunkIdOrderByOrderIndex(UUID chunkId);
}
