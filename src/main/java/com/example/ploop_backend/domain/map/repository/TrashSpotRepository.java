package com.example.ploop_backend.domain.map.repository;

import com.example.ploop_backend.domain.map.entity.TrashBin;
import com.example.ploop_backend.domain.map.entity.TrashSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrashSpotRepository extends JpaRepository<TrashSpot, Long> {
    List<TrashSpot> findAllByOrderByCreatedAtDesc();

    List<TrashSpot> findByLatitudeBetweenAndLongitudeBetween(
            double minLat, double maxLat,
            double minLng, double maxLng
    );

    @Query("""
        SELECT t FROM TrashSpot t
        WHERE t.latitude BETWEEN :minLat AND :maxLat
        AND t.longitude BETWEEN :minLng AND :maxLng
    """)
    List<TrashSpot> findWithinBounds(@Param("minLat") double minLat, @Param("maxLat") double maxLat, @Param("minLng") double minLng, @Param("maxLng")double maxLng);

}

