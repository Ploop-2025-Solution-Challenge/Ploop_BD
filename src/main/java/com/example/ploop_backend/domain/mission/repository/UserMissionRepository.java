package com.example.ploop_backend.domain.mission.repository;

import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    List<UserMission> findAllByUser(User user);
}

