package com.example.ploop_backend.dto.map;

import com.example.ploop_backend.domain.map.entity.TrashSpot;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TrashSpotDto {
    private Long id;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static TrashSpotDto from(TrashSpot spot) {
        return new TrashSpotDto(
                spot.getId(),
                spot.getLatitude(),
                spot.getLongitude(),
                spot.getImageUrl(),
                spot.getCreatedAt()
        );
    }
}


