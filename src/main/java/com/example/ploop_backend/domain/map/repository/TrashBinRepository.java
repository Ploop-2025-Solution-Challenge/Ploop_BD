package com.example.ploop_backend.domain.map.repository;

import com.example.ploop_backend.domain.map.entity.TrashBin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrashBinRepository extends JpaRepository<TrashBin, Long> {
    List<TrashBin> findAllByOrderByCreatedAtDesc();

    List<TrashBin> findByLatitudeBetweenAndLongitudeBetween(
            double minLat, double maxLat,
            double minLng, double maxLng
    );

}

