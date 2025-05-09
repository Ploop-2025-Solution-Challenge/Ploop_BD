// 📁 MissionVerificationService.java
package com.example.ploop_backend.domain.mission.service;

import com.example.ploop_backend.domain.mission.entity.Mission;
import com.example.ploop_backend.domain.mission.entity.MissionVerification;
import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.model.Category;
import com.example.ploop_backend.domain.mission.repository.MissionVerificationRepository;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.team.entity.TeamMission;
import com.example.ploop_backend.domain.user.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MissionVerificationService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://35.224.212.56:8000")
            .build();

    private final MissionVerificationRepository verificationRepository;
    private final UserMissionRepository userMissionRepository;

    public Map<String, Object> verifyMission(MultipartFile file, Long userMissionId, User user) throws IOException {
        // 받은 userMissionId로 UserMission 조회
        UserMission userMission = userMissionRepository.findById(userMissionId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저 미션입니다."));
        if (!userMission.getUser().getId().equals(user.getId())) {
            throw new SecurityException("해당 미션에 대한 권한이 없습니다.");
        }

        // 해당 미션 정보 조회
        Mission mission = userMission.getMission();
        // 미션의 검증 조건 가져오기
        Category requiredCategory = mission.getCategory();    // enum 값 (예: PLASTIC_BOTTLE)
        int requiredCount = mission.getRequiredCount();       // ex: 3


        // AI 서버에 이미지 전송
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
                .bodyToMono(String.class) // JSON 응답을 String으로 받음
                .block();

        // AI 응답 결과 파싱
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode objects = root.path("detection_results").path("objects");

        //  감지된 쓰레기 class 집계
        Map<Category, Integer> detected = new HashMap<>();
        for (JsonNode obj : objects) {
            String className = obj.path("class").asText();
            Category category = Category.mapClassNameToCategory(className);
            if (category != null) {
                detected.put(category, detected.getOrDefault(category, 0) + 1);
            }
        }
        int detectedCount = detected.getOrDefault(requiredCategory, 0);

        // 검증
        boolean isVerified;
        String reason = null;
        String message;

        if (!detected.containsKey(requiredCategory)) {
            isVerified = false;
            reason = "category";
            message = "요구된 쓰레기 종류가 감지되지 않았습니다.";
        } else if (detectedCount < requiredCount) {
            isVerified = false;
            reason = "count";
            message = "쓰레기 개수가 부족합니다. 최소 " + requiredCount + "개 필요합니다.";
        } else {
            isVerified = true;
            message = "미션 인증에 성공했습니다.";
        }

        // 결과 저장
        verificationRepository.save(MissionVerification.builder()
                .userId(user.getId())
                .totalCount(objects.size())
                .types(detected.keySet().stream()
                        .map(Enum::name)
                        .toList())
                .isVerified(isVerified)
                .verifiedAt(LocalDateTime.now())
                .build());

        // UserMission 업데이트
        userMission.setIsVerified(isVerified);
        userMissionRepository.save(userMission);

        // 응답 반환
        Map<String, Object> response = new HashMap<>();
        response.put("verified", isVerified);
        response.put("message", message);
        if (!isVerified) response.put("reason", reason);
        return response;
    }
}

