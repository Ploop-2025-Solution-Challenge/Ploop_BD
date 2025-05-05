package com.example.ploop_backend.dto.plogging;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteSummaryDto {
    private Long routeId;
    private Long userId;
    private Double timeDuration;
    private String updatedDateTime;
    private Double distanceMiles;
    private Integer trashCollectedCount;
}
