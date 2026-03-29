package com.fluenz.api.repository;

import com.fluenz.api.entity.DefaultSituation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DefaultSituationRepository extends JpaRepository<DefaultSituation, UUID> {

    List<DefaultSituation> findByTopicIdOrderByOrderIndex(UUID topicId);

    Optional<DefaultSituation> findByIdAndTopicCatalogVersionId(UUID id, UUID catalogVersionId);
}
