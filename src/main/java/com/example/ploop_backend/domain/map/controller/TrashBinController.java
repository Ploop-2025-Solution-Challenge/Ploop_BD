package com.example.ploop_backend.domain.map.controller;

import com.example.ploop_backend.domain.map.entity.TrashBin;
import com.example.ploop_backend.domain.map.service.TrashBinService;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.dto.map.TrashBinDto;
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
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/bin")
public class TrashBinController {

    private final TrashBinService trashBinService;

    // 쓰레기통 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerTrashBin(
            @AuthenticationPrincipal User user,
            @RequestParam("image") MultipartFile image,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude
    ) {
        if (user == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }
        System.out.println("🔥🔥🔥🔥🔥 TrashBin 컨트롤러 도달함");
        try {
            TrashBin saved = trashBinService.registerTrashBin(user, image, latitude, longitude);

            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.DATABASE_ERROR);
        }
    }

    // 쓰레기통 마커 전체 조회 DTO 변환
    @GetMapping
    public ResponseEntity<List<TrashBinDto>> getAllBins() {
        List<TrashBin> bins = trashBinService.getAllTrashBins();
        List<TrashBinDto> result = bins.stream()
                .map(TrashBinDto::from)   // record에서 변환 메서드 사용
                .toList();

        return ResponseEntity.ok(result);
    }

    // 현재 위치 기반 (사각형) 지도 범위 내 쓰레기통 조회
    @GetMapping("/bounds")
    public ResponseEntity<List<TrashBinDto>> getBinsWithinBounds(
            @RequestParam("minLat") double minLat,
            @RequestParam("maxLat") double maxLat,
            @RequestParam("minLng") double minLng,
            @RequestParam("maxLng") double maxLng
    ) {
        List<TrashBin> bins = trashBinService.getTrashBinsWithinBounds(minLat, maxLat, minLng, maxLng);
        List<TrashBinDto> result = bins.stream()
                .map(TrashBinDto::from)
                .toList();

        return ResponseEntity.ok(result);
    }


    // 쓰레기통 삭제 (누구나 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBin(@PathVariable("id") Long id) {
        trashBinService.deleteTrashBin(id, null);
        //return ResponseEntity.ok().build();
        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "message", "쓰레기통이 삭제되었습니다."
                ));
    }


}