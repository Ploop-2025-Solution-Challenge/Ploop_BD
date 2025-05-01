package com.example.ploop_backend.domain.plogging.controller;

import com.example.ploop_backend.dto.plogging.RouteRequestDto;
import com.example.ploop_backend.domain.plogging.service.RouteService;
import com.example.ploop_backend.dto.plogging.RouteResponseDto;
import com.example.ploop_backend.dto.plogging.RouteSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plogging")
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/end")
    public Long saveRoute(@RequestBody RouteRequestDto dto) throws Exception {
        return routeService.save(dto);
    }

    @GetMapping("/{routeId}")
    public RouteResponseDto getRoute(@PathVariable("routeId") Long routeId) throws Exception {
        return routeService.getById(routeId);
    }

    @GetMapping("/user/{userId}")
    public List<RouteSummaryDto> getRoutesByUser(@PathVariable("userId") String userId) {
        return routeService.getRoutesByUserId(userId);
    }

    @DeleteMapping("/{routeId}")
    public void deleteRoute(@PathVariable("routeId") Long routeId) {
        routeService.delete(routeId);
    }

    @GetMapping("/all")
    public List<RouteSummaryDto> getAllRoutes() {
        return routeService.getAllRoutes();
    }

}
