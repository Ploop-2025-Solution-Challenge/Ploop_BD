package com.example.ploop_backend.domain.mission.repository;

import com.example.ploop_backend.domain.mission.entity.Mission;
import com.example.ploop_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    @Query(value = "SELECT * FROM mission ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<Mission> findRandomThree();
}