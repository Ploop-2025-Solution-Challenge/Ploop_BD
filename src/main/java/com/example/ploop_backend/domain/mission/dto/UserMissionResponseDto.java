package com.example.ploop_backend.domain.mission.dto;

import com.example.ploop_backend.domain.mission.entity.Mission;
import com.example.ploop_backend.domain.mission.entity.UserMission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserMissionResponseDto {
    private Long userMissionId;
    private MissionDto mission;
    private boolean completed;
    private float progress;

    public static UserMissionResponseDto from(UserMission um) {
        return UserMissionResponseDto.builder()
                .userMissionId(um.getId())
                .mission(MissionDto.from(um.getTeamMission().getMission()))
                .completed(Boolean.TRUE.equals(um.getCompleted()))
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class MissionDto {
        private String name;
        private String description;

        public static MissionDto from(Mission mission) {
            return MissionDto.builder()
                    .name(mission.getName())
                    .description(mission.getDescription())
                    .build();
        }
    }
}
