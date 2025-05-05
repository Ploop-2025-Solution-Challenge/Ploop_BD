package com.example.ploop_backend.domain.team.repository;

import com.example.ploop_backend.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("SELECT t FROM Team t JOIN FETCH t.user1 JOIN FETCH t.user2")
    List<Team> findAllWithUsers();
}

