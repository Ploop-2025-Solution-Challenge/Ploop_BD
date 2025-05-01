package com.example.ploop_backend.domain.mission.repository;

import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    List<UserMission> findAllByUser(User user);

    // 특정 유저가 일정 기간 동안 인증 완료한 미션 개수
    int countByUserAndIsVerifiedTrueAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}

