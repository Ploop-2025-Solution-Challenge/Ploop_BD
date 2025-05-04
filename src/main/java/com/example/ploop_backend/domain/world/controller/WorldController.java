package com.example.ploop_backend.domain.world.controller;

import com.example.ploop_backend.domain.world.service.WorldService;
import com.example.ploop_backend.dto.plogging.RouteSummaryDto;
import com.example.ploop_backend.dto.world.WorldRouteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/world")
@RequiredArgsConstructor
public class WorldController {

    private final WorldService worldService;

    @GetMapping("/routes")
    public List<WorldRouteDto> getAllWorldRoutes() {
        return worldService.getAllRoutes();
    }

    @GetMapping("/routes/bounds")
    public ResponseEntity<List<WorldRouteDto>> getRoutesWithinBounds(
            @RequestParam("minLat") double minLat,
            @RequestParam("maxLat") double maxLat,
            @RequestParam("minLng") double minLng,
            @RequestParam("maxLng") double maxLng
    ) {
        List<WorldRouteDto> routes = worldService.getRoutesWithinBounds(minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(routes);
    }

}