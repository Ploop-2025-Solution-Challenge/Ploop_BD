package com.example.ploop_backend.domain.team.controller;

import com.example.ploop_backend.domain.team.dto.TeamMatchResponseDto;
import com.example.ploop_backend.domain.team.dto.TeamMissionDto;
import com.example.ploop_backend.domain.team.entity.TeamMission;
import com.example.ploop_backend.domain.team.repository.TeamMissionRepository;
import com.example.ploop_backend.domain.team.service.TeamMatchService;
import com.example.ploop_backend.domain.team.service.TeamMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamMatchService teamMatchService;
    private final TeamMissionService teamMissionService;
    private final TeamMissionRepository teamMissionRepository;

    @PostMapping("/match-weekly")
    public ResponseEntity<Void> triggerTeamMatch() {
        teamMatchService.matchWeeklyTeams(); // 매칭 실행
        teamMatchService.assignWeeklyMissions(); // 미션 할당
        return ResponseEntity.ok().build();
    }

    // 팀별 미션 조회
    @GetMapping("/{teamId}/missions")
    public ResponseEntity<List<TeamMissionDto>> getMissionsByTeam(@PathVariable(name = "teamId") Long teamId) {
        List<TeamMission> missions = teamMissionRepository.findAllByTeamId(teamId);
        List<TeamMissionDto> result = missions.stream()
                .map(TeamMissionDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // 로컬 테스트용 매칭 결과 저장 서버에서는 사용하지 않음
    @PostMapping("/match-weekly/local")
    public ResponseEntity<String> matchWeeklyLocal(@RequestBody List<TeamMatchResponseDto> matches) {
        teamMatchService.saveMatchedTeams(matches);
        return ResponseEntity.ok("로컬 DB에 매칭 결과 저장 완료 ✅");
    }
}

