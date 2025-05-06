package com.example.ploop_backend.domain.world.service;

import com.example.ploop_backend.domain.plogging.entity.Route;
import com.example.ploop_backend.domain.plogging.repository.RouteRepository;
import com.example.ploop_backend.dto.plogging.RouteSummaryDto;
import com.example.ploop_backend.dto.world.WorldRouteDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorldService {

    private final RouteRepository routeRepository;
    private final ObjectMapper objectMapper;

    public List<WorldRouteDto> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(route -> {
                    try {
                        List<List<Double>> activityRoute = objectMapper.readValue(
                                route.getActivityRouteJson(),
                                new TypeReference<>() {}
                        );

                        return WorldRouteDto.builder()
                                .routeId(route.getId())
                                .activityRoute(activityRoute)
                                .updatedDateTime(route.getUpdatedDateTime().toString())
                                .build();

                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("경로 JSON 파싱 실패: routeId = " + route.getId(), e);
                    }
                })
                .toList();
    }


    public List<WorldRouteDto> getRoutesWithinBounds(double minLat, double maxLat, double minLng, double maxLng) {
        List<Route> routes = routeRepository.findAll();

        return routes.stream()
                .filter(route -> isRouteInBounds(route, minLat, maxLat, minLng, maxLng))
                .map(route -> {
                    try {
                        List<List<Double>> activityRoute = objectMapper.readValue(
                                route.getActivityRouteJson(),
                                new TypeReference<>() {}
                        );

                        return WorldRouteDto.builder()
                                .routeId(route.getId())
                                .activityRoute(activityRoute)
                                .updatedDateTime(route.getUpdatedDateTime().toString())
                                .build();

                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("activityRoute 파싱 실패: routeId = " + route.getId(), e);
                    }
                })
                .toList();
    }

    private boolean isRouteInBounds(Route route, double minLat, double maxLat, double minLng, double maxLng) {
        try {
            // JSON 문자열을 좌표 리스트로 파싱
            List<List<Double>> points = objectMapper.readValue(
                    route.getActivityRouteJson(),
                    new com.fasterxml.jackson.core.type.TypeReference<>() {}
            );
            if (points.isEmpty()) return false; // 좌표 없음 → 제외

            // 첫 번째 좌표만 확인
            double lat = points.get(0).get(0); // [lng, lat]
            double lng = points.get(0).get(1);

            return lat >= minLat && lat <= maxLat
                    && lng >= minLng && lng <= maxLng;
        } catch (Exception e) {
            return false;
        }
    }


}

