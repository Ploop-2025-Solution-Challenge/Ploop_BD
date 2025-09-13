package com.example.ploop_backend.domain.route.controller;

import com.example.ploop_backend.domain.route.service.RouteRecommendService;
import com.example.ploop_backend.dto.map.RouteRecommendRequestDto;
import com.example.ploop_backend.dto.map.RouteRecommendResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/map/route")
@RequiredArgsConstructor
public class RouteRecommendController {

    private final RouteRecommendService routeService;

    // 프론트 → 백 : 출발지, 도착지 좌표
    @PostMapping("/recommend")
    public ResponseEntity<RouteRecommendResponseDto> recommend(
            @RequestBody RouteRecommendRequestDto request
    ) {
        RouteRecommendResponseDto response = routeService.recommend(request);
        return ResponseEntity.ok(response);
    }
}
