package com.example.ploop_backend.domain.mission.controller;

import com.example.ploop_backend.domain.mission.entity.MissionVerification;
import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.repository.MissionVerificationRepository;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.user.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mission")
public class MissionVerificationController {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://34.55.128.248:8000")
            .build();

    private final MissionVerificationRepository verificationRepository;
    private final UserMissionRepository userMissionRepository;

    @PostMapping("/verification")
    public ResponseEntity<?> verifyTrashMission(
            @RequestPart("file") MultipartFile file,
            @RequestPart("userMissionId") Long userMissionId,
            @AuthenticationPrincipal User user
    ) {
        try {
            // 1. 해당 UserMission 조회
            UserMission userMission = userMissionRepository.findById(userMissionId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 유저 미션입니다."));

            // 2. 유저 일치 여부 확인 (보안상 필수)
            if (!userMission.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("verified", false, "message", "해당 미션에 대한 권한이 없습니다."));
            }

            // 3. AI 서버에 이미지 전송
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            String jsonResponse = webClient.post()
                    .uri("/detect/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("file", resource))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 4. JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode results = root.path("detection_results");

            int total = results.path("total_detections").asInt();
            Set<String> uniqueTypes = new HashSet<>();
            for (JsonNode obj : results.path("objects")) {
                uniqueTypes.add(obj.path("class").asText());
            }

            // 5. 검증 로직
            boolean isVerified;
            String reason = null;
            String message;

            if (total < 3) {
                isVerified = false;
                reason = "count";
                message = "쓰레기 개수가 부족합니다. 최소 3개 이상이어야 합니다.";
            } else if (uniqueTypes.size() < 2) {
                isVerified = false;
                reason = "type";
                message = "쓰레기 종류가 부족합니다. 서로 다른 종류가 2개 이상이어야 합니다.";
            } else {
                isVerified = true;
                message = "미션 인증에 성공했습니다.";
            }

            // 6. 결과 저장
            verificationRepository.save(MissionVerification.builder()
                    .userId(user.getId())
                    .totalCount(total)
                    .types(new ArrayList<>(uniqueTypes))
                    .isVerified(isVerified)
                    .verifiedAt(LocalDateTime.now())
                    .build());

            userMission.setIsVerified(isVerified);
            userMissionRepository.save(userMission);

            // 7. 응답
            Map<String, Object> response = new HashMap<>();
            response.put("verified", isVerified);
            response.put("message", message);
            if (!isVerified) response.put("reason", reason);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("verified", false, "message", "미션 인증 중 오류", "detail", e.getMessage()));
        }
    }

}
