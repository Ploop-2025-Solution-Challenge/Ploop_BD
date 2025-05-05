package com.example.ploop_backend.domain.map.repository;

import com.example.ploop_backend.domain.map.entity.RouteRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RouteRecommendationRepository extends JpaRepository<RouteRecommendation, Long> {
    Optional<RouteRecommendation> findByUserIdAndDate(Long userId, LocalDate date);
}

