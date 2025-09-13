package com.example.ploop_backend.dto.route;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRouteComputeRequestDto {
    private LatLngDto current;
    private LatLngDto destination;
    private List<GeoPointWithIdDto> trash; // [{id,lat,lng}, ...]
    private List<GeoPointWithIdDto> bins;  // [{id,lat,lng}, ...]
}

