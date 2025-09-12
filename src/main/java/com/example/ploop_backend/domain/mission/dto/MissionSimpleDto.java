package com.example.ploop_backend.domain.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MissionSimpleDto {
    private Long userMissionId;
    private String category;
    private int requiredCount;
    private boolean isVerified;
}
