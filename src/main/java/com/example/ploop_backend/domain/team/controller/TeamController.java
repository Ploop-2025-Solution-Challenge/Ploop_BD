package com.example.ploop_backend.domain.team.controller;

import com.example.ploop_backend.domain.team.dto.TeamMatchRequest;
import com.example.ploop_backend.domain.team.dto.TeamMatchResponseDto;
import com.example.ploop_backend.domain.team.dto.TeamMissionDto;
import com.example.ploop_backend.domain.team.entity.TeamMission;
import com.example.ploop_backend.domain.team.repository.TeamMissionRepository;
import com.example.ploop_backend.domain.team.service.TeamMatchService;
import com.example.ploop_backend.domain.team.service.TeamMissionService;
import com.example.ploop_backend.domain.team.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// 유저 매칭 -> 팀 생성 -> 미션 배정 (내부 호출)
// 매칭 POST /match
// 주차별 팀 매칭 현황 GET/admin?week=2025-W17
@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamMatchService teamMatchService;
    private final TeamMissionService teamMissionService;
    private final TeamMissionRepository teamMissionRepository;

    // 팀 매칭
    @PostMapping("/match")
    public ResponseEntity<TeamMatchResponseDto> matchUsersToTeam(@RequestBody TeamMatchRequest request) {
        Team team = teamMatchService.createTeam(request.getUser1Id(), request.getUser2Id(), request.getWeek());
        int missionCount = teamMissionService.assignRandomMissionsToTeam(team);
        return ResponseEntity.ok(new TeamMatchResponseDto(team.getId(), missionCount, missionCount * 2));
    }

    // 팀별 미션 조회
    @GetMapping("/{teamId}/missions")
    public ResponseEntity<List<TeamMissionDto>> getMissionsByTeam(@PathVariable Long teamId) {
        List<TeamMission> missions = teamMissionRepository.findAllByTeamId(teamId);
        List<TeamMissionDto> result = missions.stream()
                .map(TeamMissionDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}

