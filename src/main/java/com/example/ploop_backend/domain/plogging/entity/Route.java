package com.example.ploop_backend.domain.plogging.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

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

    private String userId;

    private String timeDuration;

    private LocalDateTime updatedDateTime;

    private LocalDateTime startDateTime;

    private Double distanceMiles;

    private Integer trashCollectedCount;

    @Column(columnDefinition = "json")
    private String activityRouteJson; // JSON으로 직렬화된 좌표 리스트
}
