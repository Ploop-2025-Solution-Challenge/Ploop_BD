package com.example.ploop_backend.service;

import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.dto.user.UpdateUserProfileRequest;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service // UserService 클래스는 @Service 어노테이션을 사용하여 스프링의 서비스 컴포넌트로 등록
@RequiredArgsConstructor // UserService 클래스는 @Service 어노테이션을 사용하여 스프링의 서비스 컴포넌트로 등록
public class UserService {
    private final UserRepository userRepository; // UserRepository를 주입받아 사용

    public User updateProfile(User user, UpdateUserProfileRequest req) {
        user.setNickname(req.getNickname());
        user.setAge(req.getAge());
        user.setCountry(req.getCountry());
        user.setRegion(req.getRegion());
        user.setGender(req.getGender());
        return userRepository.save(user);
    }
}
