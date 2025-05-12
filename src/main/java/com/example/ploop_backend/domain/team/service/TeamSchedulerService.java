package com.example.ploop_backend.domain.team.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Service
@RequiredArgsConstructor
@RestController
public class TeamSchedulerService {

    private final TeamMatchService teamMatchService;

    // 매주 월요일 오전 9시에 실행 (cron: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 10 * * MON", zone = "UTC")
    public void scheduleWeeklyTeamMatching() {
        log.info("!!!!! every week, matching schedular start");
        try {
            teamMatchService.matchWeeklyTeams(); // 매칭 실행
            teamMatchService.assignWeeklyMissions(); // 미션 할당
            log.info("!!!!! every week, team matching finished");
        } catch (Exception e) {
            log.error("!?!?! fail matching system", e);
        }
    }
}

