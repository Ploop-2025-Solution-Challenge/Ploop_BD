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

        List<Mission> missions = missionRepository.findRandomThree();

        if (missions == null || missions.size() < 3) {
            throw new IllegalStateException("cannot find enough random missions");
        }

        for (Mission mission : missions) {
            // 팀 미션에 저장
            TeamMission teamMission = teamMissionRepository.save(
                    TeamMission.builder()
                            .team(team)
                            .mission(mission)
                            .build()
            );

            // 2. UserMission 저장 - mission 필드 포함
            userMissionRepository.save(
                    UserMission.builder()
                            .user(user1)
                            .teamMission(teamMission)
                            .mission(mission) // 미션 id를 추가
                            .build()
            );
            userMissionRepository.save(
                    UserMission.builder()
                            .user(user2)
                            .teamMission(teamMission)
                            .mission(mission) // 미션 id
                            .build()
            );

            log.info("!!!!!! UserMission saved - missionId: {}, users: {}, {}", mission.getId(), user1.getId(), user2.getId());
        }
    }

}