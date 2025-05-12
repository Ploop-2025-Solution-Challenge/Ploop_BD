package com.example.ploop_backend.domain.mission.service;

import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.entity.UserMissionHistory;
import com.example.ploop_backend.domain.mission.repository.UserMissionHistoryRepository;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMissionStoringService {

    private final UserMissionRepository userMissionRepository;
    private final UserMissionHistoryRepository userMissionHistoryRepository;

    public void storeAllVerifiedUserMissions() {
        List<UserMission> missions = userMissionRepository.findAll();

        List<UserMissionHistory> histories = missions.stream()
                .filter(UserMission::getIsVerified) // 인증된 것만
                .map(um -> UserMissionHistory.builder()
                        .user(um.getUser())
                        .teamMission(um.getTeamMission())
                        .mission(um.getMission())
                        .isVerified(true)
                        .createdAt(um.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        userMissionHistoryRepository.saveAll(histories);
    }
}
