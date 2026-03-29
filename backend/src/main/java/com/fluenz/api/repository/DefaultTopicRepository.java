package com.fluenz.api.repository;

import com.fluenz.api.entity.DefaultTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DefaultTopicRepository extends JpaRepository<DefaultTopic, UUID> {

    List<DefaultTopic> findByCatalogVersionIdOrderByOrderIndex(UUID catalogVersionId);
}
