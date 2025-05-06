package com.example.ploop_backend.domain.mission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    private Long userId; // 인증한 사용자 ID

    private int totalCount;

    @ElementCollection
    private List<String> types;

    private String imageUrl;

    private Boolean isVerified;

    private LocalDateTime verifiedAt;
}
