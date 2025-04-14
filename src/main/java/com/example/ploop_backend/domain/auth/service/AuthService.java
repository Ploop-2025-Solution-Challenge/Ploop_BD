package com.example.ploop_backend.domain.auth.service;

import com.example.ploop_backend.domain.user.repository.UserRepository;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.model.Role;
import com.example.ploop_backend.dto.auth.JwtResponseDto;
import com.example.ploop_backend.dto.auth.TokenRequestDto;

import com.example.ploop_backend.dto.auth.GoogleUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    // ID 토큰 검증, 사용자 정보 DB에 저장, JWT 발급 -> 로그인 완료

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;

    public JwtResponseDto login(TokenRequestDto request) {
        GoogleUserDto googleUser = googleTokenVerifier.verify(request.getIdToken());

        if (googleUser == null) {
            throw new RuntimeException("google user not found");
        }

        // 구글 사용자 정보로 DB에서 사용자 조회 후 존재 여부 확인, 소셜로그인이므로 비밀번호는 저장 X
        Optional<User> existingUser = userRepository.findByEmail(googleUser.getEmail());

        // 구글 사용자 정보로 DB에 사용자 저장 및 조회
        User user = existingUser.orElseGet(() -> userRepository.save(
                User.builder()
                        .email(googleUser.getEmail())
                        .googleId(googleUser.getGoogleId())
                        .name(googleUser.getName())
                        .picture(googleUser.getPicture())
                        .role(Role.USER) // 기본권한 Role.USER
                        .build()
        ));

        // JWT 발급, 인증 헤더로 사용
        String jwt = jwtService.generateJwtToken(user);
        // JWT를 JwtResponseDto(응답 DTO)에 담아 클라이언트에 반환
        return new JwtResponseDto(jwt);
    }
}
