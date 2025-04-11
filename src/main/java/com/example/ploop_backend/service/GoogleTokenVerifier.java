package com.example.ploop_backend.service;

import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;


import java.util.Collections;


@Component
public class GoogleTokenVerifier {

    @Value("${google.clientId}")
    private String clientId;


    public GoogleUser verify(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) throw new RuntimeException("Invalid ID token");

            GoogleIdToken.Payload payload = idToken.getPayload();

            return new GoogleUser(
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