package com.example.ploop_backend.domain.auth.service;

import com.example.ploop_backend.dto.auth.GoogleUserDto;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;


import java.util.Collections;


@Component
public class GoogleTokenVerifier {
    // Google OAuth ID Token을 검증하는 클래스

    @Value("${google.clientId}")
    private String clientId;


    public GoogleUserDto verify(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder( // ID 토큰 검증기 생성
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) throw new RuntimeException("Invalid ID token"); // ID 토큰 검증, idTokenString(클라이언트에서 받은 ID 토큰)을 실제 Google API에 보내서 검증
            GoogleIdToken.Payload payload = idToken.getPayload(); // 토큰 유효시 payload에 들어 있는 사용자 정보 가져오기

            return new GoogleUserDto(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Google ID token", e);
        }
    }
}