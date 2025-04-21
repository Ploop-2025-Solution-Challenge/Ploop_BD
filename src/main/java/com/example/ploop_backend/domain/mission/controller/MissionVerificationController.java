package com.example.ploop_backend.domain.mission.controller;

import com.example.ploop_backend.domain.mission.dto.MissionVerificationDto;
import com.example.ploop_backend.domain.mission.entity.MissionVerification;
import com.example.ploop_backend.domain.mission.repository.MissionVerificationRepository;
import com.example.ploop_backend.domain.mission.service.MissionVerificationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mission-verification")
@RequiredArgsConstructor
public class MissionVerificationController {

    private final MissionVerificationService missionVerificationService;
    private final MissionVerificationRepository missionVerificationRepository;

    // AI가 인증 결과를 판단하기 위한 사진 제출
    @PatchMapping("/{id}/result")
    public ResponseEntity<ResultResponse> updateVerificationResult(
            @PathVariable Long id,
            @RequestBody VerificationResultRequest request
    ) {
        missionVerificationService.applyAIResult(id, request.isVerified());
        return ResponseEntity.ok(new ResultResponse("UPDATED", request.isVerified()));
    }

    // 인증 결과 조회 (AI가 판단한 결과)
    @GetMapping("/{id}")
    public ResponseEntity<MissionVerificationDto> getVerificationResult(@PathVariable Long id) {
        MissionVerification result = missionVerificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 인증 결과가 없습니다."));
        return ResponseEntity.ok(MissionVerificationDto.from(result));
    }

    // 모든 인증 결과 조회
    @GetMapping
    public ResponseEntity<List<MissionVerificationDto>> getAllVerificationResults() {
        List<MissionVerification> all = missionVerificationRepository.findAll();
        List<MissionVerificationDto> result = all.stream()
                .map(MissionVerificationDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }


    @Data
    public static class VerificationResultRequest {
        private boolean verified;
    }

    @Data
    @AllArgsConstructor
    public static class ResultResponse {
        private String status;
        private boolean verified;
    }
}

