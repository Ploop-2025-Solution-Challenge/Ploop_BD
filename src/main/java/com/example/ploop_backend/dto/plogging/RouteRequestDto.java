package com.example.ploop_backend.dto.plogging;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRequestDto {

    private Long userId;
    private List<List<Double>> activityRoute;
    private String updatedDateTime;
    private Double timeDuration;
    private Double distanceMiles;
    private Integer trashCollectedCount;
}
