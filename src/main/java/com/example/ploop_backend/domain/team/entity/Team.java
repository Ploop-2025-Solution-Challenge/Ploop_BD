package com.example.ploop_backend.domain.team.entity;

import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_1")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_2")
    private User user2;

    private LocalDateTime createdAt = LocalDateTime.now();
}
