package com.example.ploop_backend.domain.plogging.service;

import com.example.ploop_backend.domain.plogging.entity.Route;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import com.example.ploop_backend.dto.plogging.RouteRequestDto;
import com.example.ploop_backend.domain.plogging.repository.RouteRepository;
import com.example.ploop_backend.dto.plogging.RouteResponseDto;
import com.example.ploop_backend.dto.plogging.RouteSummaryDto;
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
    private final UserRepository userRepository;


    public Long save(RouteRequestDto dto) throws Exception {
        LocalDateTime updated = LocalDateTime.parse(dto.getUpdatedDateTime());
        LocalDateTime start = updated.minusMinutes((long) (Double.parseDouble(dto.getTimeDuration()) * 60));

        String routeJson = objectMapper.writeValueAsString(dto.getActivityRoute());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));


        Route route = Route.builder()
                .user(user)
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
                .userId(route.getUser().getId())
                .activityRoute(activityRoute)
                .timeDuration(route.getTimeDuration())
                .updatedDateTime(route.getUpdatedDateTime().toString())
                .startDateTime(route.getStartDateTime().toString())
                .distanceMiles(route.getDistanceMiles())
                .trashCollectedCount(route.getTrashCollectedCount())
                .build();
    }

    // 특정 유저의 모든 routeId 조회
    public List<RouteSummaryDto> getRoutesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        List<Route> routes = routeRepository.findAllByUser(user);

        return routes.stream()
                .map(route -> RouteSummaryDto.builder()
                        .routeId(route.getId())
                        .timeDuration(route.getTimeDuration())
                        .updatedDateTime(route.getUpdatedDateTime().toString())
                        .distanceMiles(route.getDistanceMiles())
                        .trashCollectedCount(route.getTrashCollectedCount())
                        .build())
                .toList();
    }

    public void delete(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new IllegalArgumentException("해당 routeId가 존재하지 않습니다.");
        }
        routeRepository.deleteById(routeId);
    }

    // 모든 routeId 조회
    public List<RouteSummaryDto> getAllRoutes() {
        List<Route> routes = routeRepository.findAll();

        return routes.stream()
                .map(route -> RouteSummaryDto.builder()
                        .routeId(route.getId())
                        .userId(route.getUser().getId())
                        .timeDuration(route.getTimeDuration())
                        .updatedDateTime(route.getUpdatedDateTime().toString())
                        .distanceMiles(route.getDistanceMiles())
                        .trashCollectedCount(route.getTrashCollectedCount())
                        .build())
                .toList();
    }

}
