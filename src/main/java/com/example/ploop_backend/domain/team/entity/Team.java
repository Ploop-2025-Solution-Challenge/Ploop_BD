package com.example.ploop_backend.domain.team.entity;

import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String week; // ISO 형식 : 2025-W01

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_1")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_2")
    private User user2;


}
