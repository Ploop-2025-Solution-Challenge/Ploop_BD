package com.example.ploop_backend.domain.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MissionSummaryResponseDto {
    private String partnerName;
    private List<MissionSimpleDto> partnerMissions;
    private MissionSimpleDto myCurrentMission;
}
