package com.example.ploop_backend.dto.plogging;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResponseDto {
    private Long routeId;
    private String userId;
    private List<List<Double>> activityRoute;
    private String timeDuration;
    private String updatedDateTime;
    private String startDateTime;
    private Double distanceMiles;
    private Integer trashCollectedCount;
}
