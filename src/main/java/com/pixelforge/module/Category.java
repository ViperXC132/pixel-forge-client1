package com.pixelforge.module;

public enum Category {
    HUD("HUD"),
    VISUAL("Visual"),
    UTILITY("Utility"),
    PERFORMANCE("Performance"),
    MOVEMENT("Movement"),
    TRAINER("Trainer"),
    SYSTEM("System");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
