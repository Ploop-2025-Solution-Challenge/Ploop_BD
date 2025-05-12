package com.example.ploop_backend.domain.mission.entity;

import com.example.ploop_backend.domain.team.entity.TeamMission;
import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserMissionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 팀 미션 정보 (null 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_mission_id")
    private TeamMission teamMission;

    // 인증 여부
    private Boolean isVerified;

    // 미션 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    // 원래 createdAt (기록된 시점)
    private LocalDateTime createdAt;
}
