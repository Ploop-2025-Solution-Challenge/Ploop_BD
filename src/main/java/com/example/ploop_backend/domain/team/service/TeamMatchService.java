package com.example.ploop_backend.domain.team.service;

import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.team.dto.TeamMatchResponseDto;
import com.example.ploop_backend.domain.team.entity.Team;
import com.example.ploop_backend.domain.team.repository.TeamMissionRepository;
import com.example.ploop_backend.domain.team.repository.TeamRepository;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMatchService {

    private final TeamRepository teamRepository;
    private final TeamMissionService teamMissionService;
    private final UserRepository userRepository;
    private final TeamMissionRepository teamMissionRepository;
    private final UserMissionRepository userMissionRepository;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://35.224.212.56:8000")
            .build();

    @Transactional
    public void matchWeeklyTeams() {
        teamRepository.deleteAll(); // 매주 초기화

        // AI 매칭 결과 가져오기
        webClient.post()
                .uri("/match/weekly")
                .retrieve()
                .bodyToMono(Void.class)
                .block(); // 매칭 실행만

        log.info("!!!!!!matching finished!!!!");
    }


    @Transactional
    public void assignWeeklyMissions() {
        // 결과는 DB에서 직접 조회
        List<Team> teams = teamRepository.findAll();
        log.warn("!!!! Team load finished - count: {}", teams.size());

        for (Team team : teams) {
            try {
                User user1 = team.getUser1();
                User user2 = team.getUser2();

                log.info("!!!!! team ID: {}, user1: {}, user2: {}",
                        team.getId(),
                        user1 != null ? user1.getEmail() : "null",
                        user2 != null ? user2.getEmail() : "null"
                );
            } catch (Exception e) {
                log.error("!!!!! error - teamId: {}", team.getId(), e);
            }
        }

        List<Team> matchedTeams = teamRepository.findAllWithUsers();
        log.info("!!!!!! matching count: {}", matchedTeams.size());

        if (matchedTeams.isEmpty()) {
            log.warn("!!!!! no matching result in DB.");
            return;
        }
        log.info("!!!! matching count: {}", matchedTeams.size());

        matchedTeams.forEach(teamMissionService::assignRandomMissionsToTeam);
        System.out.println("????? assignRandomMissionsToTeam called??");
    }

    @Transactional
    public void saveMatchedTeams(List<TeamMatchResponseDto> matches) {
        teamRepository.deleteAll();  // 매주 초기화

        List<Team> teams = matches.stream()
                .map(m -> {
                    log.info("🔍 매칭 정보: userId1={}, userId2={}, week={}, createdAt={}",
                            m.getUserId1(), m.getUserId2(), m.getWeek(), m.getCreatedAt());

                    if (m.getUserId1() == null || m.getUserId2() == null) {
                        log.error("❌ userId가 null입니다: userId1={}, userId2={}", m.getUserId1(), m.getUserId2());
                        throw new IllegalArgumentException("userId must not be null");
                    }

                    User user1 = userRepository.findById(m.getUserId1())
                            .orElseThrow(() -> {
                                log.error("❌ User1 not found: {}", m.getUserId1());
                                return new IllegalArgumentException("User1 not found: " + m.getUserId1());
                            });

                    User user2 = userRepository.findById(m.getUserId2())
                            .orElseThrow(() -> {
                                log.error("❌ User2 not found: {}", m.getUserId2());
                                return new IllegalArgumentException("User2 not found: " + m.getUserId2());
                            });

                    log.info("✅ 사용자 조회 완료: user1={}, user2={}", user1.getEmail(), user2.getEmail());

                    return Team.builder()
                            .user1(user1)
                            .user2(user2)
                            .week(m.getWeek())
                            .createdAt(m.getCreatedAt())
                            .build();
                })
                .toList();

        log.info("💾 총 {}개의 팀 저장 시도 중", teams.size());
        teamRepository.saveAll(teams);
        log.info("✅ 팀 저장 완료");

        teams.forEach(team -> {
            log.info("🎯 팀 미션 배정 시작 - teamId: {}", team.getId());
            try {
                teamMissionService.assignRandomMissionsToTeam(team);
            } catch (Exception e) {
                log.error("💥 팀 미션 배정 중 예외 발생 - teamId: {}", team.getId(), e);
            }
        });
    }



    // 로컬 테스트용 매칭 결과 조회
    @Transactional
    public void resetWeeklyMatches() {
        userMissionRepository.deleteAll();
        teamMissionRepository.deleteAll();
        teamRepository.deleteAll();
    }

}

