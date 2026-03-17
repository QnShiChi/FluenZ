package com.fluenz.api.repository;

import com.fluenz.api.entity.SubPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubPhraseRepository extends JpaRepository<SubPhrase, UUID> {
}
