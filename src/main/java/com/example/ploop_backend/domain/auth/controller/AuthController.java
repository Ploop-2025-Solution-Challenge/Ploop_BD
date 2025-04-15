package com.example.ploop_backend.domain.auth.controller;

import com.example.ploop_backend.dto.auth.JwtResponseDto;
import com.example.ploop_backend.dto.auth.TokenRequestDto;
import com.example.ploop_backend.domain.auth.service.AuthService;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {


    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUrl;


    private final AuthService authService;
    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @GetMapping("/api/auth/login/google")
    public void googleLogin(
            HttpServletResponse response) throws IOException {

        String googleAuthUrl = UriComponentsBuilder
                .fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUrl)
                .queryParam("response_type", "code")
                .queryParam("scope", "profile email")
                .queryParam("access_type", "offline")
                .build()
                .toUriString();

        response.sendRedirect(googleAuthUrl);
    }

    @GetMapping("/auth/google/redirect")
    public ResponseEntity<JwtResponseDto> googleCallback(@RequestParam("code") String code,
                                                         HttpServletResponse response) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("code", code);
            requestBody.put("client_id", googleClientId);
            requestBody.put("client_secret", googleClientSecret);
            requestBody.put("redirect_uri", googleRedirectUrl);
            requestBody.put("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            Map<String, Object> tokenResponse = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token",
                    requestEntity,
                    Map.class
            );

            String googleIdToken = (String) tokenResponse.get("id_token");

            if (googleIdToken == null) {
                throw new RuntimeException("Google ID Token is missing");
            }

            JwtResponseDto jwtToken = authService.loginWeb(googleIdToken);

            return ResponseEntity.ok(jwtToken);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JwtResponseDto("Error: " + e.getMessage()));
        }
    }



    @PostMapping("/api/auth/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody TokenRequestDto request) {
        try {
            JwtResponseDto response = authService.login(request.getIdToken());
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}

