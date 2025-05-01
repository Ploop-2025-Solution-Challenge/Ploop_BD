package com.example.ploop_backend.domain.plogging.service;

import com.example.ploop_backend.domain.plogging.entity.Route;
import com.example.ploop_backend.dto.plogging.RouteRequestDto;
import com.example.ploop_backend.domain.plogging.repository.RouteRepository;
import com.example.ploop_backend.dto.plogging.RouteResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final ObjectMapper objectMapper; // 좌표리스트 -> JSON 문자열로 변환

    public Long save(RouteRequestDto dto) throws Exception {
        LocalDateTime updated = LocalDateTime.parse(dto.getUpdatedDateTime());
        LocalDateTime start = updated.minusMinutes((long) (Double.parseDouble(dto.getTimeDuration()) * 60));

        String routeJson = objectMapper.writeValueAsString(dto.getActivityRoute());

        Route route = Route.builder()
                .userId(dto.getUserId())
                .timeDuration(dto.getTimeDuration())
                .updatedDateTime(updated)
                .startDateTime(start)
                .distanceMiles(dto.getDistanceMiles())
                .trashCollectedCount(dto.getTrashCollectedCount())
                .activityRouteJson(routeJson)
                .build();

        return routeRepository.save(route).getId();
    }

    // 특정 routeId 조회
    public RouteResponseDto getById(Long routeId) throws Exception {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("해당 routeId가 존재하지 않습니다."));

        List<List<Double>> activityRoute = objectMapper.readValue(
                route.getActivityRouteJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, List.class)
        );

        return RouteResponseDto.builder()
                .routeId(route.getId())
                .userId(route.getUserId())
                .activityRoute(activityRoute)
                .timeDuration(route.getTimeDuration())
                .updatedDateTime(route.getUpdatedDateTime().toString())
                .startDateTime(route.getStartDateTime().toString())
                .distanceMiles(route.getDistanceMiles())
                .trashCollectedCount(route.getTrashCollectedCount())
                .build();
    }
}
