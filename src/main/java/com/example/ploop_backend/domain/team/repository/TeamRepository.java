package com.example.ploop_backend.domain.team.repository;

import com.example.ploop_backend.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {}

