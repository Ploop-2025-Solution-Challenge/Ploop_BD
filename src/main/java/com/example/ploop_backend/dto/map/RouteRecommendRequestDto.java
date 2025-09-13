package com.example.ploop_backend.dto.map;

import com.example.ploop_backend.dto.route.LatLngDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRecommendRequestDto {
    private LatLngDto current;
    private LatLngDto destination;
}
