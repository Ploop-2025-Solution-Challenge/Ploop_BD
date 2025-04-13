package com.example.ploop_backend.domain.map.repository;

import com.example.ploop_backend.domain.map.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
}
