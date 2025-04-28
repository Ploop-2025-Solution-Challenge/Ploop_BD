package com.example.ploop_backend.domain.mission.service;

import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.team.entity.TeamMission;
import com.example.ploop_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMissionService {

    private final UserMissionRepository userMissionRepository;

    public void createUserMissions(List<TeamMission> teamMissions, User... users) {
        for (User user : users) {
            for (TeamMission tm : teamMissions) {
                userMissionRepository.save(
                        UserMission.builder()
                                .user(user)
                                .teamMission(tm)
                                .build()
                );
            }
        }
    }
}

