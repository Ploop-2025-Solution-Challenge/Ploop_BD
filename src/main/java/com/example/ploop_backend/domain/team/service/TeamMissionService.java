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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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
            throw new IllegalStateException("랜덤 미션 3개를 가져오지 못했습니다.");
        }

        for (Mission mission : missions) {
            TeamMission teamMission = teamMissionRepository.save(
                    TeamMission.builder()
                            .team(team)
                            .mission(mission)
                            .build()
            );

            userMissionRepository.save(
                    UserMission.builder().user(user1).teamMission(teamMission).build()
            );
            userMissionRepository.save(
                    UserMission.builder().user(user2).teamMission(teamMission).build()
            );
        }
    }

}