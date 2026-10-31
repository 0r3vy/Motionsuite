package dev.r3vy.motionsuite.config;

import java.util.Locale;

public enum FullbrightMode {
    NIGHT_VISION("nightVision"),
    GAMMA("gamma");

    private final String id;

    FullbrightMode(String id) {
        this.id = id;
    }

    String id() {
        return id;
    }

    static FullbrightMode byId(String value, FullbrightMode fallback) {
        if (value == null) {
            return fallback;
        }
        String normalized = value.strip().toLowerCase(Locale.ROOT);
        for (FullbrightMode mode : values()) {
            if (mode.id.equals(normalized)) {
                return mode;
            }
        }
        return fallback;
    }
}
