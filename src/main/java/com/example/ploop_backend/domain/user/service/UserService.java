package com.example.ploop_backend.domain.user.service;

import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.model.Difficulty;
import com.example.ploop_backend.domain.user.model.Gender;
import com.example.ploop_backend.domain.user.model.PreferredArea;
import com.example.ploop_backend.domain.user.model.Motivation;
import com.example.ploop_backend.dto.user.UpdateUserProfileRequest;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service // UserService 클래스는 @Service 어노테이션을 사용하여 스프링의 서비스 컴포넌트로 등록
@RequiredArgsConstructor // UserService 클래스는 @Service 어노테이션을 사용하여 스프링의 서비스 컴포넌트로 등록
public class UserService {
    private final UserRepository userRepository; // UserRepository를 주입받아 사용

    public User updateProfile(User user, UpdateUserProfileRequest req) {
        user.setNickname(req.getNickname());
        user.setAge(req.getAge());
        user.setCountry(req.getCountry());
        user.setRegion(req.getRegion());

        // 프론트에서 받아온 문자열 → Enum 변환
        Gender gender = Gender.fromLabel(req.getGender());
        Difficulty difficulty = Difficulty.fromLabel(req.getDifficulty());
        Motivation motivation = Motivation.fromLabel(req.getMotivation());
        List<PreferredArea> preferredArea = req.getPreferredArea().stream()
                .map(PreferredArea::fromLabel)
                .collect(Collectors.toList());

        user.setGender(gender); // 추가된 필드 (enum 타입)
        user.setDifficulty(difficulty); // 추가된 필드
        user.setMotivation(motivation); // 추가된 필드
        user.setPreferredArea(preferredArea); // 추가된 필드

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}
