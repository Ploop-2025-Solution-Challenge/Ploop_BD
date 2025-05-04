package com.example.ploop_backend.domain.plogging.repository;

import com.example.ploop_backend.domain.plogging.entity.Route;
import com.example.ploop_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findAllByUser(User user);
    List<Route> findAllByUserAndStartDateTimeBetween(User user, LocalDateTime start, LocalDateTime end);
}