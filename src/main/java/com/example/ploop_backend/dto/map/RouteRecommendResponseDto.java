package com.example.ploop_backend.dto.map;

import com.example.ploop_backend.dto.route.LatLngDto;
import com.example.ploop_backend.dto.route.RouteDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.util.List;

// 백 -> 프론트 응답 바디
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRecommendResponseDto {
    private LatLngDto current;
    private LatLngDto destination;

    private boolean success;   // 예: true
    private String message;    // 예: "ok"

    private List<LatLngDto> waypoints; // [{lat,lng}, ...]

    private RouteDto route;     // {encodedPolyline, distanceMeters, duration}
}