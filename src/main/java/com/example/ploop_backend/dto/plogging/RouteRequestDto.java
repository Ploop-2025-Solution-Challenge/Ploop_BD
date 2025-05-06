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
    private List<List<Double>> activityRoute;   // JSON 문자열을 디코딩해서 사용.
    private String updatedDateTime;
    private Double timeDuration;
    private Double distanceMiles;
    private Integer trashCollectedCount;
}
