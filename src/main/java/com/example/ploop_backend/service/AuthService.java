package com.example.ploop_backend.service;

import com.example.ploop_backend.domain.user.repository.UserRepository;
import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.model.Role;
import com.example.ploop_backend.dto.auth.JwtResponseDto;
import com.example.ploop_backend.dto.auth.TokenRequestDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier; // 따로 만들 거야

    public JwtResponseDto login(TokenRequestDto request) {
        GoogleUser googleUser = googleTokenVerifier.verify(request.getIdToken());

        Optional<User> existingUser = userRepository.findByEmail(googleUser.getEmail());

        User user = existingUser.orElseGet(() -> userRepository.save(
                User.builder()
                        .email(googleUser.getEmail())
                        .googleId(googleUser.getGoogleId())
                        .name(googleUser.getName())
                        .picture(googleUser.getPicture())
                        .role(Role.USER)
                        .build()
        ));

        String jwt = jwtService.generateToken(user);
        return new JwtResponseDto(jwt);
    }
}
