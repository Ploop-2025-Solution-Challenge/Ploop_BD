package com.example.ploop_backend.domain.mission.controller;

import com.example.ploop_backend.domain.mission.dto.MissionSimpleDto;
import com.example.ploop_backend.domain.mission.dto.MissionSummaryResponseDto;
import com.example.ploop_backend.domain.mission.dto.UserMissionResponseDto;
import com.example.ploop_backend.domain.mission.entity.MissionVerification;
import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.mission.service.ImageUploadService;
import com.example.ploop_backend.domain.mission.service.MissionVerificationService;
import com.example.ploop_backend.domain.team.entity.Team;
import com.example.ploop_backend.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/missions")
@RequiredArgsConstructor
public class MissionController {

    private final UserMissionRepository userMissionRepository;
    private final ImageUploadService imageUploadService;
    private final MissionVerificationService missionVerificationService;

    // 유저의 전체 미션 조회 (3개)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyMissions(@AuthenticationPrincipal User user) {
        List<UserMission> missions = userMissionRepository.findAllByUser(user);
        List<UserMissionResponseDto> result = missions.stream()
                .map(UserMissionResponseDto::from)
                .collect(Collectors.toList());
        Team team = missions.isEmpty() ? null : missions.get(0).getTeamMission().getTeam();
        String partnerNickname = null;
        Long teamId = null;

        if (team != null) {
            teamId = team.getId();
            User partner = team.getUser1().getId().equals(user.getId()) ? team.getUser2() : team.getUser1();
            partnerNickname = partner.getNickname();
        }

        return ResponseEntity.ok(Map.of(
                "teamId", teamId,
                "partnerName", partnerNickname,
                "missions", result
        ));
    }

    // 유저의 특정 미션 조회
    @GetMapping("/{userMissionId}")
    public ResponseEntity<UserMissionResponseDto> getMissionDetail(@PathVariable("userMissionId") Long userMissionId) {
        UserMission mission = userMissionRepository.findById(userMissionId).orElseThrow();

        UserMissionResponseDto dto = UserMissionResponseDto.from(mission);
        return ResponseEntity.ok(dto);
    }


    // 미션 요약 조회: 파트너, 파트너 미션, 내 이번 주 미션 포함
    @GetMapping("/summary")
    public ResponseEntity<?> getMissionSummary(@AuthenticationPrincipal User user) {
        List<UserMission> myMissions = userMissionRepository.findAllByUser(user);
        if (myMissions.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "message", "아직 미션이 없습니다."
            ));
        }

        Team team = myMissions.get(0).getTeamMission().getTeam();
        User partner = team.getUser1().getId().equals(user.getId()) ? team.getUser2() : team.getUser1();
        String partnerImageUrl = partner.getPicture();


        // 파트너 미션
        List<UserMission> partnerMissions = userMissionRepository.findAllByUser(partner);

        // 파트너 미션 DTO 리스트
        List<MissionSimpleDto> partnerMissionDtos = partnerMissions.stream()
                .filter(m -> m.getTeamMission() != null && m.getTeamMission().getMission() != null)
                .map(m -> MissionSimpleDto.builder()
                        .userMissionId(m.getId())
                        .name(m.getTeamMission().getMission().getName())
                        .isVerified(Boolean.TRUE.equals(m.getIsVerified()))
                        .build()
                )
                .toList();

        // 내 미션 DTO 리스트
        List<MissionSimpleDto> myMissionsDtos = myMissions.stream()
                .filter(m -> m.getTeamMission() != null && m.getTeamMission().getMission() != null)
                .map(m -> MissionSimpleDto.builder()
                        .userMissionId(m.getId())
                        .name(m.getTeamMission().getMission().getName())
                        .isVerified(Boolean.TRUE.equals(m.getIsVerified()))
                        .build()
                )
                .toList();


        return ResponseEntity.ok(MissionSummaryResponseDto.builder()
                .partnerName(partner.getNickname())
                .partnerImageUrl(partnerImageUrl)
                .partnerMissions(partnerMissionDtos)
                .myMissions(myMissionsDtos)
                .build());
    }

}
