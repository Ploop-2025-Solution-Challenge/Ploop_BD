package com.example.ploop_backend.domain.team.entity;

import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyMatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    private User user2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private LocalDateTime createdAt = LocalDateTime.now();
}
