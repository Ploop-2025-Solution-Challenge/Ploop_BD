package com.example.ploop_backend.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RouteRecommendationResponseDto {
    private List<List<Double>> recommendationRoute;
    private String motivation;
    //private List<List<Double>> activityRoute;
}
