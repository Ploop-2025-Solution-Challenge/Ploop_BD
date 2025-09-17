package com.example.ploop_backend.domain.team.service;

import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.mission.service.UserMissionStoringService;
import com.example.ploop_backend.domain.team.repository.TeamMissionRepository;
import com.example.ploop_backend.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Service
@RequiredArgsConstructor
public class TeamSchedulerService {

    private final TeamMatchService teamMatchService;
    private final TeamRepository teamRepository;
    private final UserMissionRepository userMissionRepository;
    private final TeamMissionRepository teamMissionRepository;
    private final UserMissionStoringService userMissionStoringService;

    // 한국 기준 매주 목요일 오전 11시 50분에 실행 → 미션 백업
    @Scheduled(cron = "0 50 8 * * THU", zone = "Asia/Seoul")
    public void backupUserMissions() {
        log.info("===== [Scheduler] START: Weekly mission backup job triggered =====");
        try {
            userMissionStoringService.storeAllVerifiedUserMissions();
            log.info("[Scheduler] ✅ UserMission -> UserMissionHistory stored");
        } catch (Exception e) {
            log.error("[Scheduler] ❌ Failed to backup user missions", e);
        }
        log.info("===== [Scheduler] END: Weekly mission backup job =====");
    }

    // 한국 기준 매주 목요일 오후 12시 10분 실행 → 팀 미션 할당
    @Scheduled(cron = "0 10 9 * * THU", zone = "Asia/Seoul")
    public void assignWeeklyMissions() {
        log.info("===== [Scheduler] START: Weekly mission assignment job triggered =====");
        long start = System.currentTimeMillis();

        try {
            teamMatchService.assignWeeklyMissions();
            log.info("[Scheduler] ✅ Weekly missions assigned successfully");
        } catch (Exception e) {
            log.error("[Scheduler] ❌ Failed to assign weekly missions", e);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("===== [Scheduler] END: Weekly mission assignment job (took {} ms) =====", elapsed);
    }

    /*// 한국 기준 매주 목요일 오전 3시 13분 실행 (UTC 수요일 18:13)
    @Scheduled(cron = "0 3 4 * * WED", zone = "Asia/Seoul")
    public void scheduleWeeklyTeamMatching() {
        log.info("===== [Scheduler] START: Weekly team matching job triggered =====");
        long start = System.currentTimeMillis();

        try { // 이전 미션 백업
            log.debug("[Scheduler] Step 1: Storing all verified user missions...");
            userMissionStoringService.storeAllVerifiedUserMissions();
            log.info("[Scheduler] ✅ UserMission -> UserMissionHistory stored");
            // 이전 팀 미션, 유저 미션 삭제

            // 팀 매칭 실행

            log.debug("[Scheduler] Step 3: Assigning new weekly missions...");
            teamMatchService.assignWeeklyMissions();
            log.info("[Scheduler] ✅ Weekly missions assigned successfully");

            long elapsed = System.currentTimeMillis() - start;
            log.info("===== [Scheduler] END: Weekly team matching finished (took {} ms) =====", elapsed);

        } catch (Exception e) {
            log.error("[Scheduler] ❌ Failed to run weekly team matching", e);
        }
    }*/
}

