package com.fluenz.api.repository;

import com.fluenz.api.entity.DefaultChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DefaultChunkRepository extends JpaRepository<DefaultChunk, UUID> {

    List<DefaultChunk> findBySituationIdOrderByOrderIndex(UUID situationId);
}
