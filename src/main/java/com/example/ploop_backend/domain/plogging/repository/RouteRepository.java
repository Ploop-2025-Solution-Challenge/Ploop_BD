package com.example.ploop_backend.domain.plogging.repository;

import com.example.ploop_backend.domain.plogging.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}