package com.example.ploop_backend.controller;

import com.example.ploop_backend.domain.team.service.TeamMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamAdminController {

    private final TeamMatchService teamMatchService;

    @PostMapping("/reset-weekly")
    public ResponseEntity<String> resetWeeklyTeamMatches() {
        teamMatchService.resetWeeklyMatches();
        return ResponseEntity.ok("✅ 팀 매칭이 초기화되었습니다.");
    }
}
