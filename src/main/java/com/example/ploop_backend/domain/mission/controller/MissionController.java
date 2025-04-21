// 📁 MissionController.java
package com.example.ploop_backend.domain.mission.controller;

import com.example.ploop_backend.domain.mission.dto.UserMissionResponseDto;
import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.mission.service.ImageUploadService;
import com.example.ploop_backend.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/missions")
@RequiredArgsConstructor
public class MissionController {

    private final UserMissionRepository userMissionRepository;
    private final ImageUploadService imageUploadService;

    // 유저의 전체 미션 조회 (3개)
    @GetMapping
    public ResponseEntity<List<UserMissionResponseDto>> getMyMissions(@AuthenticationPrincipal User user) {
        List<UserMission> missions = userMissionRepository.findAllByUser(user);
        List<UserMissionResponseDto> result = missions.stream()
                .map(UserMissionResponseDto::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    // 특정 미션 상세 조회
    @GetMapping("/{userMissionId}")
    public ResponseEntity<UserMission> getMissionDetail(@PathVariable Long userMissionId) {
        UserMission mission = userMissionRepository.findById(userMissionId).orElseThrow();
        return ResponseEntity.ok(mission);
    }

    // 인증 사진 제출 (AI가 판단할 사진 제출)
    @PostMapping("/{userMissionId}/verify")
    public ResponseEntity<?> submitVerification(
            @PathVariable Long userMissionId,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            String imageUrl = imageUploadService.saveImageToCloud(image);
            return ResponseEntity.ok(Map.of(
                    "userMissionId", userMissionId,
                    "status", "PENDING",
                    "imageUrl", imageUrl,
                    "message", "사진이 업로드되었습니다."
            ));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "500 error : 파일 업로드 중 오류가 발생했습니다.",
                    "detail", e.getMessage()
            ));
        }
    }

}
