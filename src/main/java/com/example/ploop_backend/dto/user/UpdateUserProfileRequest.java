package com.example.ploop_backend.dto.user;

import com.example.ploop_backend.domain.user.model.Gender;
import lombok.Getter;

@Getter
public class UpdateUserProfileRequest {
    private String nickname;
    private Integer age;
    private String country;
    private String region;
    private Gender gender;
}
