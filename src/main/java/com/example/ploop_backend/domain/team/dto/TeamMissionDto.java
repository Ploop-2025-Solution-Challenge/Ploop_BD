package com.example.ploop_backend.domain.team.dto;

import com.example.ploop_backend.domain.mission.entity.Mission;
import com.example.ploop_backend.domain.team.entity.TeamMission;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class TeamMissionDto {
    private Long missionId;
    private String name;
    private String description;

    public static TeamMissionDto from(TeamMission entity) {
        Mission mission = entity.getMission();
        return TeamMissionDto.builder()
                .missionId(mission.getId())
                .name(mission.getName())
                .description(mission.getDescription())
                .build();
    }
}
