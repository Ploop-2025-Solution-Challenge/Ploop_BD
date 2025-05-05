package com.example.ploop_backend.domain.map.controller;

import com.example.ploop_backend.domain.map.service.RouteRecommendationService;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.dto.map.RouteRecommendationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map/route")
@RequiredArgsConstructor
public class RouteRecommendationController {

    private final RouteRecommendationService routeService;

    @GetMapping("/recommendation/bounds")
    public ResponseEntity<RouteRecommendationResponseDto> getRouteRecommendation(
            @AuthenticationPrincipal User user,
            @RequestParam("minLat") double minLat,
            @RequestParam("maxLat") double maxLat,
            @RequestParam("minLng") double minLng,
            @RequestParam("maxLng") double maxLng
    ) {
        RouteRecommendationResponseDto dto = routeService.recommendRoute(user.getId(), minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(dto);
    }
}
