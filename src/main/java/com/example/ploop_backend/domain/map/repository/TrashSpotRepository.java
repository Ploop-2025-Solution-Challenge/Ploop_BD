package com.example.ploop_backend.domain.map.repository;

import com.example.ploop_backend.domain.map.entity.TrashBin;
import com.example.ploop_backend.domain.map.entity.TrashSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrashSpotRepository extends JpaRepository<TrashSpot, Long> {
    List<TrashSpot> findAllByOrderByCreatedAtDesc();

    List<TrashSpot> findByLatitudeBetweenAndLongitudeBetween(
            double minLat, double maxLat,
            double minLng, double maxLng
    );
}

