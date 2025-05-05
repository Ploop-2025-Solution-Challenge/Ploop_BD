package com.example.ploop_backend.domain.team.service;

import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.team.dto.TeamMatchDto;
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
            .baseUrl("http://34.59.147.153:8000")
            .build();

    @Transactional
    public void matchAndSaveWeeklyTeams() {
        teamRepository.deleteAll();  // 매주 초기화

        // AI 매칭 결과 가져오기
        webClient.post()
                .uri("/match/weekly")
                .retrieve()
                .bodyToMono(TeamMatchDto.class)
                .block();
        log.info("!!!!!!matching finished!!!!");

        // 결과는 DB에서 직접 조회
        List<Team> matchedTeams = teamRepository.findAllWithUsers();
        log.info("!!!!!! matching count: {}", matchedTeams.size());

        if (matchedTeams.isEmpty()) {
            log.warn("!!!!! no matching result in DB.");
            return;
        }
        log.info("🔍 조회된 매칭 수: {}", matchedTeams.size());

        matchedTeams.forEach(teamMissionService::assignRandomMissionsToTeam);
    }

    // 로컬 테스트용 매칭 결과 저장
    @Transactional
    public void saveMatchedTeams(List<TeamMatchResponseDto> matches) {
        teamRepository.deleteAll();  // 매주 초기화

        List<Team> teams = matches.stream()
                .map(m -> {
                    User user1 = userRepository.findById(m.getUserId1())
                            .orElseThrow(() -> new IllegalArgumentException("User1 not found: " + m.getUserId1()));
                    User user2 = userRepository.findById(m.getUserId2())
                            .orElseThrow(() -> new IllegalArgumentException("User2 not found: " + m.getUserId2()));

                    return Team.builder()
                            .user1(user1)
                            .user2(user2)
                            .week(m.getWeek())
                            .createdAt(m.getCreatedAt())
                            .build();
                })
                .toList();

        teamRepository.saveAll(teams);
        teams.forEach(teamMissionService::assignRandomMissionsToTeam);
    }


    // 로컬 테스트용 매칭 결과 조회
    @Transactional
    public void resetWeeklyMatches() {
        userMissionRepository.deleteAll();
        teamMissionRepository.deleteAll();
        teamRepository.deleteAll();
    }

}

