package com.example.ploop_backend.dto.route;

import lombok.*;

// 프론트/백 응답의 route 오브젝트 공통 형태
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteDto {
    private String encodedPolyline; // 예: "a~l~Fjk~uOwHJy@P"
    private int distanceMeters;     // 예: 1820
    private String duration;        // 예: "600s"
}
