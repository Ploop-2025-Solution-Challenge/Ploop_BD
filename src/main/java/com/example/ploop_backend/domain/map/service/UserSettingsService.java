package com.example.ploop_backend.domain.map.service;

import com.example.ploop_backend.domain.map.entity.UserSettings;
import com.example.ploop_backend.domain.map.repository.UserSettingsRepository;
import com.example.ploop_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public boolean toggleTrashBinVisibility(User user) {
        UserSettings settings = userSettingsRepository.findById(user.getId())
                .orElse(UserSettings.builder().user(user).isTrashBinVisible(true).build());

        settings.setTrashBinVisible(!settings.isTrashBinVisible());
        userSettingsRepository.save(settings);

        return settings.isTrashBinVisible();
    }

    public boolean getTrashBinVisibility(User user) {
        return userSettingsRepository.findById(user.getId())
                .map(UserSettings::isTrashBinVisible)
                .orElse(true); // 기본값: 보이게
    }
}

