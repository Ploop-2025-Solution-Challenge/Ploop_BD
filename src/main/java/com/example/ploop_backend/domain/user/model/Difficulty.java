package com.example.ploop_backend.domain.user.model;

import java.util.Arrays;

public enum Difficulty {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced"),;

    private final String label;

    Difficulty(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Difficulty fromLabel(String label) {
        return Arrays.stream(Difficulty.values())
                .filter(d -> d.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown difficulty: " + label));
    }
}

