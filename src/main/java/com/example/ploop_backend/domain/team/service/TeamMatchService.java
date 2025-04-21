package com.example.ploop_backend.domain.team.service;

import com.example.ploop_backend.domain.team.entity.Team;
import com.example.ploop_backend.domain.team.repository.TeamRepository;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamMatchService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional
    public Team createTeam(Long user1Id, Long user2Id, String week) {
        validateUserNotAlreadyInTeam(user1Id, week);
        validateUserNotAlreadyInTeam(user2Id, week);

        User user1 = userRepository.findById(user1Id).orElseThrow();
        User user2 = userRepository.findById(user2Id).orElseThrow();

        Team team = Team.builder()
                .user1(user1)
                .user2(user2)
                .week(week)
                .build();

        return teamRepository.save(team);
    }

    public void validateUserNotAlreadyInTeam(Long userId, String week) {
        boolean exists = teamRepository.existsByUser1_IdAndWeek(userId, week)
                || teamRepository.existsByUser2_IdAndWeek(userId, week);
        if (exists) {
            throw new IllegalArgumentException("해당 유저는 이미 이번 주 팀에 소속되어 있습니다.");
        }
    }
}

