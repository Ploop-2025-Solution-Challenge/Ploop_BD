package com.example.ploop_backend.domain.mission.repository;

import com.example.ploop_backend.domain.mission.entity.UserMissionHistory;
import com.example.ploop_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMissionHistoryRepository extends JpaRepository<UserMissionHistory, Long> {

    // 누적 미션 수 조회 (인증된 것만)
    int countByUserAndIsVerifiedTrue(User user);

    // 기간 내 인증된 미션 수 조회
    int countByUserAndIsVerifiedTrueAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    // 최근 인증 기록 정렬 조회 등도 필요하면 추가 가능
    List<UserMissionHistory> findAllByUserOrderByCreatedAtDesc(User user);
}
