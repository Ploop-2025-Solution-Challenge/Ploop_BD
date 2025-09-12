package com.example.ploop_backend.domain.mission.controller;

import com.example.ploop_backend.domain.mission.dto.MissionSimpleDto;
import com.example.ploop_backend.domain.mission.dto.MissionSummaryResponseDto;
import com.example.ploop_backend.domain.mission.dto.UserMissionResponseDto;
import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.mission.service.ImageUploadService;
import com.example.ploop_backend.domain.mission.service.MissionVerificationService;
import com.example.ploop_backend.domain.team.entity.Team;
import com.example.ploop_backend.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/missions")
@RequiredArgsConstructor
@Slf4j
public class MissionController {

    private final UserMissionRepository userMissionRepository;
    private final ImageUploadService imageUploadService;            // (다른 엔드포인트에서 사용 예정이면 유지)
    private final MissionVerificationService missionVerificationService; // (다른 엔드포인트에서 사용 예정이면 유지)

    /**
     * 유저의 전체 미션 조회 (3개) — NULL 안전 & 메시지 기반 응답
     * - 미션 없으면: {"message":"미션 없음"}
     * - 팀/파트너 없으면: {"message":"파트너 없음", teamId:null, partnerName:"파트너 없음", missions:[...]}
     * - 정상: {teamId, partnerName, missions}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyMissions(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 정보 없음"));
        }

        List<UserMission> missions = userMissionRepository.findAllByUser(user);

        if (missions.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "미션 없음"));
        }

        // DTO 변환 (null-safe 필터링 포함)
        List<UserMissionResponseDto> result = missions.stream()
                .map(UserMissionResponseDto::from)
                .toList();

        // 팀/파트너 계산 (첫 미션 기준)
        Team team = null;
        try {
            team = missions.get(0).getTeamMission() != null ? missions.get(0).getTeamMission().getTeam() : null;
        } catch (Exception e) {
            log.warn("팀 정보를 가져오는 중 문제가 발생했습니다: {}", e.getMessage());
        }

        Long teamId = null;
        String partnerName = null;
        String message = null;

        if (team != null) {
            teamId = team.getId();

            User user1 = team.getUser1();
            User user2 = team.getUser2();

            User partner = null;
            if (user1 != null && user2 != null) {
                partner = user1.getId().equals(user.getId()) ? user2 : user1;
            }

            if (partner != null) {
                partnerName = partner.getNickname();
            } else {
                partnerName = "파트너 없음";
                message = "파트너 없음";
            }
        } else {
            partnerName = "파트너 없음";
            message = "파트너 없음";
        }

        // HashMap 사용: null value 허용
        Map<String, Object> response = new HashMap<>();
        if (message != null) response.put("message", message);
        response.put("teamId", teamId);
        response.put("partnerName", partnerName);
        response.put("missions", result);

        return ResponseEntity.ok(response);
    }

    /**
     * 유저의 특정 미션 조회 — 존재하지 않으면 메시지 반환
     * - 미션 없으면: {"message":"미션 없음"}
     * - 정상: UserMissionResponseDto
     */
    @GetMapping("/{userMissionId}")
    public ResponseEntity<?> getMissionDetail(@AuthenticationPrincipal User user,
                                              @PathVariable("userMissionId") Long userMissionId) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 정보 없음"));
        }

        return userMissionRepository.findById(userMissionId)
                .map(UserMissionResponseDto::from)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Map.of("message", "미션 없음")));
    }

    /**
     * 미션 요약 조회: 파트너, 파트너 미션, 내 이번 주 미션 포함
     * - 내 미션이 없으면: {"message":"아직 미션이 없습니다."}
     * - 팀/파트너 없으면: {"message":"파트너 없음", partnerMissions:[], myMissions:[...], partnerName:"파트너 없음", partnerImageUrl:null}
     * - 정상: MissionSummaryResponseDto
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getMissionSummary(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 정보 없음"));
        }

        List<UserMission> myMissions = userMissionRepository.findAllByUser(user);
        if (myMissions.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "아직 미션이 없습니다."));
        }

        // 팀 & 파트너
        Team team = null;
        try {
            team = myMissions.get(0).getTeamMission() != null ? myMissions.get(0).getTeamMission().getTeam() : null;
        } catch (Exception e) {
            log.warn("팀 정보를 가져오는 중 문제가 발생했습니다: {}", e.getMessage());
        }

        User partner = null;
        if (team != null && team.getUser1() != null && team.getUser2() != null) {
            partner = team.getUser1().getId().equals(user.getId()) ? team.getUser2() : team.getUser1();
        }

        // 내 미션 DTO 리스트
        List<MissionSimpleDto> myMissionsDtos = myMissions.stream()
                .filter(m -> m.getTeamMission() != null && m.getTeamMission().getMission() != null)
                .map(m -> MissionSimpleDto.builder()
                        .userMissionId(m.getId())
                        .requiredCount(m.getTeamMission().getMission().getRequiredCount())
                        .category(String.valueOf(m.getTeamMission().getMission().getCategory()))
                        .isVerified(Boolean.TRUE.equals(m.getIsVerified()))
                        .build())
                .toList();

        // 파트너 없으면 메시지로 처리 + 최소 정보 제공
        if (partner == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "파트너 없음");
            response.put("partnerName", "파트너 없음");
            response.put("partnerImageUrl", null);
            response.put("partnerMissions", List.of());
            response.put("myMissions", myMissionsDtos);
            return ResponseEntity.ok(response);
        }

        // 파트너 미션
        List<UserMission> partnerMissions = userMissionRepository.findAllByUser(partner);

        List<MissionSimpleDto> partnerMissionDtos = partnerMissions.stream()
                .filter(m -> m.getTeamMission() != null && m.getTeamMission().getMission() != null)
                .map(m -> MissionSimpleDto.builder()
                        .userMissionId(m.getId())
                        .requiredCount(m.getTeamMission().getMission().getRequiredCount())
                        .category(String.valueOf(m.getTeamMission().getMission().getCategory()))
                        .isVerified(Boolean.TRUE.equals(m.getIsVerified()))
                        .build())
                .toList();

        String partnerImageUrl = partner.getPicture();

        // 정상 케이스: 기존 DTO로 응답
        return ResponseEntity.ok(MissionSummaryResponseDto.builder()
                .partnerName(partner.getNickname())
                .partnerImageUrl(partnerImageUrl)
                .partnerMissions(partnerMissionDtos)
                .myMissions(myMissionsDtos)
                .build());
    }
}
