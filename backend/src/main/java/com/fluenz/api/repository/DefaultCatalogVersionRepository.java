package com.fluenz.api.repository;

import com.fluenz.api.entity.DefaultCatalogVersion;
import com.fluenz.api.entity.enums.DefaultCatalogVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DefaultCatalogVersionRepository extends JpaRepository<DefaultCatalogVersion, UUID> {

    Optional<DefaultCatalogVersion> findFirstByStatusOrderByVersionNumberDesc(DefaultCatalogVersionStatus status);

    List<DefaultCatalogVersion> findAllByOrderByVersionNumberDesc();

    Optional<DefaultCatalogVersion> findFirstByPublishedTrueOrderByVersionNumberDesc();
}
