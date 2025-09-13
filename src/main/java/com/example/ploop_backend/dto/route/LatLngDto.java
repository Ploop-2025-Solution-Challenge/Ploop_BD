package com.example.ploop_backend.dto.route;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LatLngDto {
    private double lat;
    private double lng;

    public double[] toArray() { return new double[]{lat, lng}; }
    public static LatLngDto of(double lat, double lng) { return new LatLngDto(lat, lng); }
}
