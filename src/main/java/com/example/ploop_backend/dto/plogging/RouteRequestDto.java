package com.example.ploop_backend.dto.plogging;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRequestDto {

    private String userId;
    private List<List<Double>> activityRoute;
    private String timeDuration;
    private String updatedDateTime;
    private Double distanceMiles;
    private Integer trashCollectedCount;
}
