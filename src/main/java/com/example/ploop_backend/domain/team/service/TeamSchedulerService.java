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
@RestController
public class TeamSchedulerService {

    private final TeamMatchService teamMatchService;
    private final TeamRepository teamRepository;
    private final UserMissionRepository userMissionRepository;
    private final TeamMissionRepository teamMissionRepository;
    private final UserMissionStoringService userMissionStoringService;

    // 한국 기준 매주 월요일 오전 9시에 실행 (cron: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 10 18 * * WED", zone = "UTC")
    public void scheduleWeeklyTeamMatching() {
        log.info("!!!!! every week, storing mission count sum, matching schedular start");
        try {
            // 이전 미션 백업
            userMissionStoringService.storeAllVerifiedUserMissions();
            log.info("!!!!! UserMission -> UserMissionHistory stored");


            // 팀 매칭 실행
            //teamMatchService.matchWeeklyTeams();
            teamRepository.deleteAll(); // 매주 초기화
            userMissionRepository.deleteAll(); // 매주 초기화
            teamMissionRepository.deleteAll(); // 매주 초기화
            // 미션 할당
            teamMatchService.assignWeeklyMissions();
            log.info("!!!!! every week, team matching finished");
        } catch (Exception e) {
            log.error("!?!?! fail matching system", e);
        }
    }
}

