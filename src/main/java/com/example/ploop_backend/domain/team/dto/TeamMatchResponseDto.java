package com.example.ploop_backend.domain.team.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamMatchResponseDto {
    private Long teamId;
    //private int missionCount;
    //private int userMissionsCreated;

    private Long userId1;
    private Long userId2;
}

