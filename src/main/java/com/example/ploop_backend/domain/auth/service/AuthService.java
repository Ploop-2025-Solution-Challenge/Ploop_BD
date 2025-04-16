package com.example.ploop_backend.domain.auth.service;

import com.example.ploop_backend.domain.user.repository.UserRepository;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.model.Role;
import com.example.ploop_backend.dto.auth.JwtResponseDto;
import com.example.ploop_backend.dto.auth.TokenRequestDto;

import com.example.ploop_backend.dto.auth.GoogleUserDto;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    // ID 토큰 검증, 사용자 정보 DB에 저장, JWT 발급 -> 로그인 완료

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public JwtResponseDto login(String code) {
        GoogleUserDto googleUser = googleTokenVerifier.verify(code);

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
        return new JwtResponseDto(jwt);
    }

    public JwtResponseDto loginWeb(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new RuntimeException("Invalid ID Token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");
        String googleId = payload.getSubject();

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user = existingUser.orElseGet(() -> userRepository.save(
                User.builder()
                        .email(email)
                        .googleId(googleId)
                        .name(name)
                        .picture(picture)
                        .role(Role.USER)
                        .build()
        ));

        String jwt = jwtService.generateJwtToken(user);
        return new JwtResponseDto(jwt);
    }
}
