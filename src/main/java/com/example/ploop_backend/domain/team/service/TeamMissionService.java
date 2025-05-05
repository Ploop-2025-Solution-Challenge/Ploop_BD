package com.example.ploop_backend.domain.team.service;

import com.example.ploop_backend.domain.mission.entity.Mission;
import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.repository.MissionRepository;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.team.entity.Team;
import com.example.ploop_backend.domain.team.entity.TeamMission;
import com.example.ploop_backend.domain.team.repository.TeamMissionRepository;
import com.example.ploop_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMissionService {

    private final MissionRepository missionRepository;
    private final TeamMissionRepository teamMissionRepository;
    private final UserMissionRepository userMissionRepository;

    @Transactional
    public void assignRandomMissionsToTeam(Team team) {
        User user1 = team.getUser1();
        User user2 = team.getUser2();

        log.info("👥 팀 {}의 유저1: {}, 유저2: {}", team.getId(), user1 != null ? user1.getId() : "null", user2 != null ? user2.getId() : "null");

        List<Mission> missions = missionRepository.findRandomThree();
        log.info("🎯 랜덤 미션 개수: {}", missions.size());

        if (missions == null || missions.size() < 3) {
            throw new IllegalStateException("랜덤 미션 3개를 가져오지 못했습니다.");
        }

        for (Mission mission : missions) {
            TeamMission teamMission = teamMissionRepository.save(
                    TeamMission.builder()
                            .team(team)
                            .mission(mission)
                            .build()
            );
            log.info("✅ TeamMission 저장 - teamId: {}, missionId: {}", team.getId(), mission.getId());


            userMissionRepository.save(
                    UserMission.builder().user(user1).teamMission(teamMission).build()
            );
            userMissionRepository.save(
                    UserMission.builder().user(user2).teamMission(teamMission).build()
            );
            log.info("👤 UserMission 저장 - missionId: {}, users: {}, {}", mission.getId(), user1.getId(), user2.getId());
        }
    }

}