package com.example.ploop_backend.domain.map.controller;

import com.example.ploop_backend.domain.map.entity.TrashBin;
import com.example.ploop_backend.domain.map.service.TrashBinService;
import com.example.ploop_backend.domain.map.service.UserSettingsService;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.dto.map.TrashBinMarkerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/bin")
public class TrashBinController {

    private final TrashBinService trashBinService;
    private final UserSettingsService userSettingsService;

    // 쓰레기통 등록
    @PostMapping
    public ResponseEntity<TrashBin> registerTrashBin(
            @AuthenticationPrincipal User user,
            @RequestParam("image") MultipartFile image,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude
    ) {
        TrashBin saved = trashBinService.registerTrashBin(user, image, latitude, longitude);
        return ResponseEntity.ok(saved);
    }

    /* // 쓰레기통 전체 조회
    @GetMapping
    public ResponseEntity<List<TrashBin>> getAllBins() {
        return ResponseEntity.ok(trashBinService.getAllTrashBins());
    }*/

    // 쓰레기통 마커 전체 조회 DTO 변환
    @GetMapping
    public ResponseEntity<List<TrashBinMarkerDto>> getAllBins() {
        List<TrashBin> bins = trashBinService.getAllTrashBins();
        List<TrashBinMarkerDto> result = bins.stream()
                .map(bin -> new TrashBinMarkerDto(
                        bin.getId(),
                        bin.getLatitude(),
                        bin.getLongitude(),
                        bin.getImageUrl()))
                .toList();

        return ResponseEntity.ok(result);
    }


    // 쓰레기통 삭제 (누구나 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBin(@PathVariable Long id) {
        trashBinService.deleteTrashBin(id, null);
        //return ResponseEntity.ok().build();
        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "message", "쓰레기통이 삭제되었습니다."
                ));
    }

    // 쓰레기통 보이기/숨기기 토글
    @PatchMapping("/visible")
    public ResponseEntity<Boolean> toggleVisibility(@AuthenticationPrincipal User user) {
        boolean visible = userSettingsService.toggleTrashBinVisibility(user);
        return ResponseEntity.ok(visible);
    }

    // 쓰레기통 보이기/숨기기 상태 조회
    @GetMapping("/visible")
    public ResponseEntity<Boolean> getVisibility(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userSettingsService.getTrashBinVisibility(user));
    }

}