package com.example.ploop_backend.domain.mission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MissionVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_mission_id")

    private UserMission userMission;

    private String imageUrl;

    private Boolean isVerified;

    private LocalDateTime submittedAt;

    private LocalDateTime verifiedAt;
}
