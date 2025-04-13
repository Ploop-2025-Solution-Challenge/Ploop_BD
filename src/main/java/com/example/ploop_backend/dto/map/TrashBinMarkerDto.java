package com.example.ploop_backend.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrashBinMarkerDto {
    private Long id;
    private double latitude;
    private double longitude;
    private String imageUrl;
}
