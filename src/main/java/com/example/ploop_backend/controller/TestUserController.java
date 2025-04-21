// 📁 TestUserController.java
package com.example.ploop_backend.controller;

import com.example.ploop_backend.domain.mission.repository.MissionVerificationRepository;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.team.repository.TeamMissionRepository;
import com.example.ploop_backend.domain.team.repository.TeamRepository;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.model.Difficulty;
import com.example.ploop_backend.domain.user.model.Gender;
import com.example.ploop_backend.domain.user.model.Motivation;
import com.example.ploop_backend.domain.user.model.Role;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestUserController {

    private final UserRepository userRepository;
    private final MissionVerificationRepository missionVerificationRepository;
    private final UserMissionRepository userMissionRepository;
    private final TeamMissionRepository teamMissionRepository;
    private final TeamRepository teamRepository;

    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/create-user")
    public ResponseEntity<User> createTestUser(@RequestBody TestUserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .nickname(request.getNickname())
                .googleId("test-google-id-" + System.currentTimeMillis())
                .role(Role.USER)
                .difficulty(Difficulty.BEGINNER)
                .gender(Gender.FEMALE)
                .motivation(Motivation.AWARENESS)
                .picture(null)
                .build();

        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/create-batch")
    public ResponseEntity<List<User>> createBatchUsers(@RequestParam(name = "count", defaultValue = "10") int count) {
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            User user = User.builder()
                    .email("test" + i + "@example.com")
                    .name("테스트" + i)
                    .nickname("테스트유저" + i)
                    .googleId("test-google-id-" + System.currentTimeMillis() + i)
                    .role(Role.USER)
                    .difficulty(Difficulty.BEGINNER)
                    .gender(Gender.FEMALE)
                    .motivation(Motivation.AWARENESS)
                    .picture(null)
                    .build();
            users.add(user);
        }

        return ResponseEntity.ok(userRepository.saveAll(users));
    }

    @DeleteMapping("/delete-dummy-users")
    public ResponseEntity<String> deleteDummyUsers() {
        List<User> dummyUsers = userRepository.findAll().stream()
                .filter(user -> user.getEmail().startsWith("test"))
                .toList();

        userRepository.deleteAll(dummyUsers);
        return ResponseEntity.ok("더미 유저 " + dummyUsers.size() + "명 삭제 완료");
    }

    @DeleteMapping("/delete-dummy-all")
    public ResponseEntity<String> deleteAllDummyData() {
        // 순서 중요: verification → user_mission → team_mission → team → user
        List<User> dummyUsers = userRepository.findAll().stream()
                .filter(user -> user.getEmail().startsWith("test"))
                .toList();

        missionVerificationRepository.deleteAll();
        userMissionRepository.deleteAll();
        teamMissionRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll(dummyUsers);

        return ResponseEntity.ok("더미 유저 및 관련 데이터 전체 삭제 완료");
    }

    @Data
    public static class TestUserRequest {
        private String email;
        private String name;
        private String nickname;
    }
}
