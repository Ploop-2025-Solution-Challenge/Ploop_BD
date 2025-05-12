package com.example.ploop_backend.domain.auth.service;

import com.example.ploop_backend.config.GoogleProperties;
import com.example.ploop_backend.dto.auth.GoogleUserDto;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;


import java.util.Base64;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class GoogleTokenVerifier {
    // Google OAuth ID Token을 검증하는 클래스

    private final GoogleProperties googleProperties;

    @PostConstruct
    public void check() {
        System.out.println("!!!! google.clientIds = " + googleProperties.getClientIds()); // 구글 클라이언트 ID 확인
    }

    public GoogleUserDto verify(String idTokenString) {
        System.out.println("!!!! Received ID token: " + idTokenString);

        try {
            List<String> clientIds = googleProperties.getClientIds();
            System.out.println("!!!! Allowed clientIds: " + clientIds);

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(clientIds)
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            String[] parts = idTokenString.split("\\.");
            if (parts.length == 3) {
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                System.out.println("🧾 Decoded payload = " + payloadJson);
            }

            if (idToken == null) {
                System.err.println("!!!! ID token is null — failed verification.");
                throw new RuntimeException("Invalid ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            System.out.println("!!!! Token verified successfully");
            System.out.println("   - email: " + payload.getEmail());
            System.out.println("   - aud: " + payload.getAudience());
            System.out.println("   - azp: " + payload.getAuthorizedParty());

            return new GoogleUserDto(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );
        } catch (Exception e) {
            System.err.println("!!!! Exception during ID token verification: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to verify Google ID token", e);
        }
    }

}