package com.fluenz.api.repository;

import com.fluenz.api.entity.LearnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LearnerProfileRepository extends JpaRepository<LearnerProfile, UUID> {
}
