package com.example.ploop_backend.domain.team.service;

import com.example.ploop_backend.domain.mission.entity.Mission;
import com.example.ploop_backend.domain.mission.repository.MissionRepository;
import com.example.ploop_backend.domain.team.entity.Team;
import com.example.ploop_backend.domain.team.entity.TeamMission;
import com.example.ploop_backend.domain.team.repository.TeamMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TeamMissionService {

    private final MissionRepository missionRepository;
    private final TeamMissionRepository teamMissionRepository;

    public List<TeamMission> assignRandomMissions(Team team) {
        List<Mission> missions = missionRepository.findRandomThree();

        return missions.stream().map(mission ->
                teamMissionRepository.save(
                        TeamMission.builder()
                                .team(team)
                                .mission(mission)
                                .build())
        ).toList();
    }
}

