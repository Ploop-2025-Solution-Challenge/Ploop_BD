package com.example.ploop_backend.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
