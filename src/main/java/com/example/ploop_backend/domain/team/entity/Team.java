package com.example.ploop_backend.domain.team.entity;

import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        // 주차당 하나의 팀을 보장
        // user1_id와 user2_id는 서로 다른 유저여야 함
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user1_id", "week"}),
                @UniqueConstraint(columnNames = {"user2_id", "week"})
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String week; // ISO 형식 : 2025-W01

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_1")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_2")
    private User user2;

    private LocalDateTime createdAt = LocalDateTime.now();
}
