package com.example.ploop_backend.domain.mission.repository;

import com.example.ploop_backend.domain.mission.entity.MissionVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionVerificationRepository extends JpaRepository<MissionVerification, Long> {
    // Custom query methods can be defined here if needed
}
