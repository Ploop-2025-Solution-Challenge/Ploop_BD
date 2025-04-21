package com.example.ploop_backend.domain.mission.model;

import lombok.Getter;

@Getter
public enum Category {
    EMPTY_CAN("빈 캔"),
    PLASTIC_BOTTLE("플라스틱 병"),
    BOTTLE_CAP("페트병 뚜껑"),
    PAPER_CUP("종이컵"),
    VINYL_BAG("비닐봉지");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }
}