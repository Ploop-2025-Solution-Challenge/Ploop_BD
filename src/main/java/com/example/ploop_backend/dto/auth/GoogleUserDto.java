package com.example.ploop_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleUserDto {
    private String googleId;
    private String email;
    private String name;
    private String picture;
}

