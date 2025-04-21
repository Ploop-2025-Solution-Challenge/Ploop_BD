package com.example.ploop_backend.dto.map;

import com.example.ploop_backend.domain.map.entity.TrashSpot;

public record TrashSpotDto(Long id, double latitude, double longitude, String imageUrl) {
    public static TrashSpotDto from(TrashSpot spot) {
        return new TrashSpotDto(spot.getId(), spot.getLatitude(), spot.getLongitude(), spot.getImageUrl());
    }
}

