package com.fluenz.api.repository;

import com.fluenz.api.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, UUID> {

    List<Chunk> findBySituationIdOrderByOrderIndex(UUID situationId);
}
