package com.example.ploop_backend.domain.user.model;

import java.util.Arrays;

public enum Gender {
    FEMALE("Female"),
    MALE("Male"),
    UNKNOWN("Prefer not to say");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Gender fromLabel(String label) {
        return Arrays.stream(Gender.values())
                .filter(g -> g.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown gender: " + label));
    }
}
