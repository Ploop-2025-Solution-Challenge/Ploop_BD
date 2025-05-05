package com.example.ploop_backend.domain.plogging.entity;

import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double timeDuration;

    private Double distanceMiles;

    private LocalDateTime updatedDateTime;

    private LocalDateTime startDateTime;

    private Integer trashCollectedCount;

    @Column(columnDefinition = "json")
    private String activityRouteJson; // JSON으로 직렬화된 좌표 리스트
}
