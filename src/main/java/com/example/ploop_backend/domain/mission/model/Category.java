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

    public static Category mapClassNameToCategory(String className) {
        className = className.toLowerCase();

        if (className.contains("can")) return Category.EMPTY_CAN;
        if (className.contains("bottle cap")) return Category.BOTTLE_CAP;
        if (className.contains("bottle")) return Category.PLASTIC_BOTTLE;
        if (className.contains("cup")) return Category.PAPER_CUP;
        if (className.contains("bag") || className.contains("vinyl")) return Category.VINYL_BAG;

        return null;
    }
}