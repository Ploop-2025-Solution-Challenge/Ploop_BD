package com.example.ploop_backend.domain.mission.entity;

import com.example.ploop_backend.domain.team.entity.TeamMission;
import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_mission_id")
    private TeamMission teamMission;

    private Boolean completed = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
