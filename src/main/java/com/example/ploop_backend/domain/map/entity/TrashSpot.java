package com.example.ploop_backend.domain.map.entity;

import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrashSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double latitude;
    private double longitude;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    private User reportedBy; // 쓰레기 구역을 신고한 유저

    private LocalDateTime createdAt;
}

