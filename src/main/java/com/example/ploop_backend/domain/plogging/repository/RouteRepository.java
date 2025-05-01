package com.example.ploop_backend.domain.plogging.repository;

import com.example.ploop_backend.domain.plogging.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findAllByUserId(String userId);
}