package com.example.ploop_backend.domain.team.entity;

import com.example.ploop_backend.domain.mission.entity.Mission;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TeamMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    private LocalDateTime createdAt = LocalDateTime.now();
}
