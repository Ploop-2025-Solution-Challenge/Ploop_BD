package com.example.ploop_backend.dto.world;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorldRouteDto {
    private Long routeId;
    private String activityRouteJson;
    private String updatedDateTime;
}