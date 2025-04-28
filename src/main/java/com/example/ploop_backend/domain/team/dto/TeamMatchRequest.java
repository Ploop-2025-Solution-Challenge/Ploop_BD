package com.example.ploop_backend.domain.team.dto;

import lombok.Data;

@Data
public class TeamMatchRequest {
    private Long user1Id;
    private Long user2Id;
    private String week;
}
