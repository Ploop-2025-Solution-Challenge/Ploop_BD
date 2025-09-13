package com.example.ploop_backend.dto.route;

import lombok.*;

// id가 포함된 위도, 경도 좌표
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoPointWithIdDto {
    private String id;
    private double lat;
    private double lng;
}