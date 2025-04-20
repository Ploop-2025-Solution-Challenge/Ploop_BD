package com.example.ploop_backend.domain.user.model;

import java.util.Arrays;

public enum PreferredArea {
    NATURE("Nature (riverside paths, forest trails, parks)"),
    ALLEYS("Urban alleys"),
    CULTURE("Historic sites / Cultural streets"),
    COAST("Coastal areas"),
    CAMPUS("Around university campuses"),
    HIDDEN("Hidden gems");

    private final String label;

    PreferredArea(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PreferredArea fromLabel(String label) {
        return Arrays.stream(PreferredArea.values())
                .filter(p -> p.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown preference: " + label));
    }
}

