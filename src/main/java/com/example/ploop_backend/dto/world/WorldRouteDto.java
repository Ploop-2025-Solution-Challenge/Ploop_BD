package com.example.ploop_backend.dto.world;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldRouteDto {
    private Long routeId;
    private List<List<Double>> activityRoute; // JSON 문자열을 디코딩해서 사용.
    private String updatedDateTime;
}