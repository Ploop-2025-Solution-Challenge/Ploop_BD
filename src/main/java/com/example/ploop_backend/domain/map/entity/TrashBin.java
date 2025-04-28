package com.example.ploop_backend.domain.map.entity;

import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrashBin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long latitude;
    private Long longitude;
    private String imageUrl; // 사진 URL 경로

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User createdBy; // 쓰레기통을 생성한 유저

    private LocalDateTime createdAt; // 쓰레기통 생성 시간 자동등록
}