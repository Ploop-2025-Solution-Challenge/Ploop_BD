package com.example.ploop_backend.domain.user.model;

import java.util.Arrays;

public enum Motivation {
    COMMUNITY("To contribute to the local community"),
    STRESS("To relieve stress"),
    TREND("To participate in social trends or challenges"),
    SELF("For self-development"),
    AWARENESS("To raise social awareness"),
    HEALTH("For health improvement");

    private final String label;

    Motivation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Motivation fromLabel(String label) {
        return Arrays.stream(Motivation.values())
                .filter(m -> m.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown motivation: " + label));
    }
}

