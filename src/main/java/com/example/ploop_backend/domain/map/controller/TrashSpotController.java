package com.example.ploop_backend.domain.map.controller;

import com.example.ploop_backend.domain.map.entity.TrashSpot;
import com.example.ploop_backend.domain.map.service.TrashSpotService;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.dto.map.TrashSpotDto;
import com.example.ploop_backend.global.error.ErrorCode;
import com.example.ploop_backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/trashspot")
public class TrashSpotController {

    private final TrashSpotService trashSpotService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerTrashSpot(
            @AuthenticationPrincipal User user,
            @RequestParam("image") MultipartFile image,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude
    ) {
        System.out.println("🔥🔥🔥🔥🔥 TrashSpot 컨트롤러 도달함");
        try {
            TrashSpot spot = trashSpotService.registerTrashSpot(user, image, latitude, longitude);

            return ResponseEntity.ok(spot);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.DATABASE_ERROR);
        }
    }

    // 쓰래기 구역 전체 조회 DTO 변환
    @GetMapping
    public ResponseEntity<List<TrashSpotDto>> getAllSpots() {
        List<TrashSpot> spots = trashSpotService.getAllTrashSpots();
        List<TrashSpotDto> result = spots.stream()
                .map(TrashSpotDto::from) // record에서 변환 메서드 사용
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/bounds")
    public ResponseEntity<List<TrashSpotDto>> getTrashSpotsWithinBounds(
            @RequestParam("minLat") double minLat,
            @RequestParam("maxLat") double maxLat,
            @RequestParam("minLng") double minLng,
            @RequestParam("maxLng") double maxLng
    ) {
        List<TrashSpot> spots = trashSpotService.getTrashSpotsWithinBounds(minLat, maxLat, minLng, maxLng);
        List<TrashSpotDto> result = spots.stream()
                .map(TrashSpotDto::from)
                .toList();

        return ResponseEntity.ok(result);
    }


}
