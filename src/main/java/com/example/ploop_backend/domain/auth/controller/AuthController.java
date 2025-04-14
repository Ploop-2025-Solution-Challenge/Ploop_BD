package com.example.ploop_backend.domain.auth.controller;

import com.example.ploop_backend.dto.auth.JwtResponseDto;
import com.example.ploop_backend.dto.auth.TokenRequestDto;
import com.example.ploop_backend.domain.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody TokenRequestDto request) {
        JwtResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

