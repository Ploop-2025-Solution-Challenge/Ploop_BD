package com.example.ploop_backend.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

// AI에서 받은 경로 계산 응답 바디.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRouteComputeResponseDto {
    // 요청 에코
    private LatLngDto current;
    private LatLngDto destination;
    private List<GeoPointWithIdDto> trash;
    private List<GeoPointWithIdDto> bins;

    // 결과 공통
    private boolean success;      // 예: true
    private String message;       // 예: "ok"
    private List<LatLngDto> waypoints;

    @JsonProperty("routeSummary")
    private RouteSummaryDto route;
}

