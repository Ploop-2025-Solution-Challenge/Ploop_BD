package com.example.ploop_backend.domain.plogging.controller;

import com.example.ploop_backend.dto.plogging.RouteRequestDto;
import com.example.ploop_backend.domain.plogging.service.RouteService;
import com.example.ploop_backend.dto.plogging.RouteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

}
