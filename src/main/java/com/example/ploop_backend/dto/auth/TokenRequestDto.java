package com.example.ploop_backend.dto.auth;

public class TokenRequestDto {
    private String idToken;

    public TokenRequestDto(String idToken) {
        this.idToken = idToken; // Flutter에서 보내는 Google ID Token

    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
