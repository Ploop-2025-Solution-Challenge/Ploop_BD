package com.example.ploop_backend.domain.team.repository;

import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.team.entity.TeamMission;
import com.example.ploop_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMissionRepository extends JpaRepository<TeamMission, Long> {
    // make findAllByTeamId
    List<TeamMission> findAllByTeamId(Long teamId);
}

