package com.example.ploop_backend.domain.team.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamSchedulerService {

    private final TeamMatchService teamMatchService;

    // 매주 월요일 오전 9시에 실행 (cron: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 0 * * MON", zone = "UTC")
    public void scheduleWeeklyTeamMatching() {
        log.info("✅ 매주 팀 매칭 스케줄러 시작");
        try {
            teamMatchService.matchAndSaveWeeklyTeams();
            log.info("✅ 매주 팀 매칭 완료");
        } catch (Exception e) {
            log.error("❌ 매주 팀 매칭 실패", e);
        }
    }
}

