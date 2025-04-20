package com.example.ploop_backend.dto.user;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateUserProfileRequest {
    private String nickname;
    private Integer age;
    private String gender; //// Gender enum이 아닌, 프론트에서 보내는 문자열 그대로 받음
    private String country;
    private String region;

    private String difficulty;
    private String motivation;

    private List<String> preferredArea;
}
