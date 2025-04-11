package com.example.ploop_backend.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleUser {
    private String googleId;
    private String email;
    private String name;
    private String picture;
}

