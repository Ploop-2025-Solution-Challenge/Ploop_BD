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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionVerificationService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://35.224.212.56:8000")
            .build();

    private final MissionVerificationRepository verificationRepository;
    private final UserMissionRepository userMissionRepository;

    public Map<String, Object> verifyMission(MultipartFile file, Long userMissionId, User user) throws IOException {
        // 1) 유저의 미션 조회 / 권한 체크
        // 받은 userMissionId로 UserMission 조회
        UserMission userMission = userMissionRepository.findById(userMissionId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저 미션입니다."));
        if (!userMission.getUser().getId().equals(user.getId())) {
            throw new SecurityException("해당 미션에 대한 권한이 없습니다.");
        }

        // 2) 미션 요구사항
        // 해당 미션 정보 조회
        Mission mission = userMission.getMission();
        // 미션의 검증 조건 가져오기
        Category requiredCategory = mission.getCategory();    // enum 값 (예: PLASTIC_BOTTLE)
        int requiredCount = mission.getRequiredCount();       // ex: 3


        // 3) AI 서버에 이미지 전송
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        String jsonResponse = webClient.post()
                .uri("/detect")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", resource))
                .retrieve()
                .bodyToMono(String.class) // JSON 응답을 String으로 받음
                .block();

        // 4) AI 응답 결과 파싱 -> 응답 구조가 바뀜
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode resultsNode = root.path("results");

        if (resultsNode.isMissingNode() || !resultsNode.isObject()) {
            throw new RuntimeException("감지된 쓰레기가 없습니다. (results 없음)");
        }

        //  감지된 쓰레기 class 개수 집계
        Map<Category, Integer> detected = new HashMap<>();
        int totalCount = 0; // 전체 감지된 쓰레기 개수

        // AI 서버가 반환한 각 객체의 class를 내부 카테고리(enum)로 매핑하여 집계
        Iterator<Map.Entry<String, JsonNode>> fields = resultsNode.fields(); //
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            int count = entry.getValue().asInt(0);
            totalCount += Math.max(count, 0);

            if (count <= 0) continue;

            // 외부 키를 내부 카테고리(enum)로 매핑
            Category category = Category.mapClassNameToCategory(key); // Category.mapClassNameToCategory("can") -> Category.CAN
            if (category != null) { // 매핑 성공한 카테고리만 집계
                detected.put(category, detected.getOrDefault(category, 0) + count);
            }
        }

        // 요구된 카테고리의 감지 개수
        int detectedCount = detected.getOrDefault(requiredCategory, 0);
        // detectedCount = detected.getOrDefault(CAN, 0) = 5 -> 개수 조건 충족

        // 요구 외에 감지된 카테고리 목록(표시용)
        List<String> otherDetected =
                detected.entrySet().stream()
                        .filter(e -> !e.getKey().equals(requiredCategory) && e.getValue() > 0)
                        .map(e -> displayName(e.getKey()))
                        .collect(Collectors.toList());
        String othersBracket = otherDetected.isEmpty()
                ? ""
                : " [" + String.join(", ", otherDetected) + "]";


        // 5) 검증
        boolean isVerified;
        String reason = null;
        String message;

        String reqDisp = displayName(requiredCategory);

        if (detectedCount == 0) {
            isVerified = false;
            reason = "category";
            // 요구 종류 미감지 + (대신 기타 감지 항목 안내)
            message = "요구된 쓰레기 종류 '" + reqDisp + "'가 감지되지 않았습니다."
                    + (otherDetected.isEmpty() ? "" : " 대신" + othersBracket + "가 감지되었습니다.");
        } else if (detectedCount < requiredCount) {
            isVerified = false;
            reason = "count";
            // 개수 부족 + (기타 감지 항목 안내)
            message = "쓰레기 개수가 부족합니다. '" + reqDisp + "' 최소 " + requiredCount + "개 필요합니다. (현재 " + detectedCount + "개)"
                    + (otherDetected.isEmpty() ? "" : " 또한 요구 항목 외" + othersBracket + "가 감지되었습니다.");
        } else {
            isVerified = true;
            // 성공 + (기타 감지 항목 안내)
            message = "미션 인증에 성공했습니다."
                    + (otherDetected.isEmpty() ? "" : " 요구 항목 '" + reqDisp + "' 이외의 다른 쓰레기" + othersBracket + "가 감지되었습니다.");
        }


        // 6) 결과 저장
        verificationRepository.save(MissionVerification.builder()
                .userId(user.getId())
                .totalCount(totalCount)
                .types(detected.keySet().stream()
                        .map(Enum::name)
                        .toList())
                .isVerified(isVerified)
                .verifiedAt(LocalDateTime.now())
                .build());

        // 7) UserMission 업데이트
        userMission.setIsVerified(isVerified);
        userMissionRepository.save(userMission);

        // 8) 응답 반환
        Map<String, Object> response = new HashMap<>();
        response.put("verified", isVerified);
        response.put("message", message);
        if (!isVerified) response.put("reason", reason);
        return response;
    }
    private String displayName(Category category) {
        // enum 이름을 소문자/스네이크 유지 가정.
        return category.name().toLowerCase();
    }
}

