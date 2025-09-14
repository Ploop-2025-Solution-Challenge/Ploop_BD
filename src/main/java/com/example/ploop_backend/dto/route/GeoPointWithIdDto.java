package com.example.ploop_backend.dto.route;

import lombok.*;

// AI에 넘길 쓰레기/쓰레기통 포인트(id, lat, lng).
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoPointWithIdDto {
    private String id;
    private double lat;
    private double lng;
}