package com.example.ploop_backend.controller;


import com.example.ploop_backend.domain.map.entity.TrashSpot;
import com.example.ploop_backend.domain.map.repository.TrashSpotRepository;
import com.example.ploop_backend.domain.map.service.TrashSpotService;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import com.example.ploop_backend.dto.map.TrashSpotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestTrashSpotController {

    private final TrashSpotService trashSpotService;
    private final UserRepository userRepository;
    private final TrashSpotRepository trashSpotRepository;

    @GetMapping("/all-trashspots")
    public ResponseEntity<List<TrashSpotDto>> getAllTrashSpots() {
        List<TrashSpot> spots = trashSpotRepository.findAll();
        List<TrashSpotDto> dtos = spots.stream()
                .map(TrashSpotDto::from)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/create-trashspots")
    public ResponseEntity<String> createDummyTrashSpots(
            @RequestParam(name = "count", defaultValue = "20") int count,
            @RequestParam(name = "baseLat", defaultValue = "36.0") double baseLat,
            @RequestParam(name = "baseLng", defaultValue = "127.0") double baseLng
    ) {
        User user = userRepository.findById(52L).orElseThrow(); // 테스트 사용자

        for (int i = 0; i < count; i++) {
            double lat = baseLat + Math.random() * 0.01;
            double lng = baseLng + Math.random() * 0.01;

            MultipartFile dummyImage = new MockMultipartFile(
                    "dummy.jpg", "dummy.jpg", "image/jpeg", new byte[0]
            );

            try {
                trashSpotService.registerTrashSpot(user, dummyImage, lat, lng);
            } catch (Exception e) {
                System.err.println("등록 실패: " + e.getMessage());
            }
        }

        return ResponseEntity.ok("✅ 더미 TrashSpot " + count + "개 생성 완료");
    }

    @DeleteMapping("delete-trashspot/{id}")
    public ResponseEntity<String> deleteTrashSpot(@PathVariable Long id) {
        if (!trashSpotRepository.existsById(id)) {
            return ResponseEntity.status(404).body("❌ TrashSpot 없음: ID = " + id);
        }

        trashSpotRepository.deleteById(id);
        return ResponseEntity.ok("✅ 삭제 완료: ID = " + id);
    }
}
