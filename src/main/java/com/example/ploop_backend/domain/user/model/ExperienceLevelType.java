package com.example.ploop_backend.domain.user.model;

public enum ExperienceLevelType {
    BEGINNER("BEGINNER"),
    INTERMEDIATE("INTERMEDIATE"),
    ADVANCED("ADVANCED");

    private final String value;

    ExperienceLevelType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExperienceLevelType fromValue(String value) {
        for (ExperienceLevelType type : ExperienceLevelType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown experience level type: " + value);
    }
}
