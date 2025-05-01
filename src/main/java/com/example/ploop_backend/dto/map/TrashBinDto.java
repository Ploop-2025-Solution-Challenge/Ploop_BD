package com.example.ploop_backend.dto.map;

import com.example.ploop_backend.domain.map.entity.TrashBin;
import com.example.ploop_backend.domain.map.entity.TrashSpot;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TrashBinDto {
    private Long id;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static TrashBinDto from(TrashBin bin) {
        return new TrashBinDto(
                bin.getId(),
                bin.getLatitude(),
                bin.getLongitude(),
                bin.getImageUrl(),
                bin.getCreatedAt()
        );
    }
}
