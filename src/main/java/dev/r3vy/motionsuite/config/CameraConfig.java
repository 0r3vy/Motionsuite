package dev.r3vy.motionsuite.config;

import com.google.gson.JsonObject;

public record CameraConfig(
        boolean enabled,
        double speed,
        double firstPersonSpeed,
        int exceptionDuration,
        double maxOffset,
        boolean crystalGuard,
        double guardMaxOffset,
        double guardSpeed) {
    static final double DEFAULT_SPEED = 0.25;
    static final double DEFAULT_FIRST_PERSON_SPEED = 0.3;
    static final double DEFAULT_MAX_OFFSET = 4.0;
    static final double DEFAULT_GUARD_MAX_OFFSET = 1.0;
    static final double DEFAULT_GUARD_SPEED = 0.7;
    static final int DEFAULT_DURATION = 1200;

    public static final double MIN_SPEED = 0.02;
    static final double MAX_SPEED = 1.0;
    static final double MAX_OFFSET_LIMIT = 64.0;
    public static final double MIN_GUARD_OFFSET = 0.1;
    public static final double MAX_GUARD_OFFSET = 8.0;
    public static final int MIN_DURATION = 100;
    public static final int MAX_DURATION = 10000;

    static CameraConfig defaults() {
        return new CameraConfig(true, DEFAULT_SPEED, DEFAULT_FIRST_PERSON_SPEED, DEFAULT_DURATION,
                DEFAULT_MAX_OFFSET, true, DEFAULT_GUARD_MAX_OFFSET, DEFAULT_GUARD_SPEED);
    }

    public CameraConfig withEnabled(boolean value) {
        return new CameraConfig(value, speed, firstPersonSpeed, exceptionDuration, maxOffset,
                crystalGuard, guardMaxOffset, guardSpeed);
    }

    public CameraConfig withSpeed(double value) {
        return new CameraConfig(enabled, clamp(value, MIN_SPEED, MAX_SPEED, DEFAULT_SPEED), firstPersonSpeed,
                exceptionDuration, maxOffset, crystalGuard, guardMaxOffset, guardSpeed);
    }

    public CameraConfig withFirstPersonSpeed(double value) {
        return new CameraConfig(enabled, speed, clamp(value, MIN_SPEED, MAX_SPEED, DEFAULT_FIRST_PERSON_SPEED),
                exceptionDuration, maxOffset, crystalGuard, guardMaxOffset, guardSpeed);
    }

    public CameraConfig withExceptionDuration(int value) {
        return new CameraConfig(enabled, speed, firstPersonSpeed,
                Math.max(MIN_DURATION, Math.min(MAX_DURATION, value)), maxOffset, crystalGuard,
                guardMaxOffset, guardSpeed);
    }

    public CameraConfig withMaxOffset(double value) {
        return new CameraConfig(enabled, speed, firstPersonSpeed, exceptionDuration,
                clamp(value, 0.0, MAX_OFFSET_LIMIT, DEFAULT_MAX_OFFSET), crystalGuard, guardMaxOffset, guardSpeed);
    }

    public CameraConfig withCrystalGuard(boolean value) {
        return new CameraConfig(enabled, speed, firstPersonSpeed, exceptionDuration, maxOffset, value,
                guardMaxOffset, guardSpeed);
    }

    public CameraConfig withGuardMaxOffset(double value) {
        return new CameraConfig(enabled, speed, firstPersonSpeed, exceptionDuration, maxOffset, crystalGuard,
                clamp(value, MIN_GUARD_OFFSET, MAX_GUARD_OFFSET, DEFAULT_GUARD_MAX_OFFSET), guardSpeed);
    }

    public CameraConfig withGuardSpeed(double value) {
        return new CameraConfig(enabled, speed, firstPersonSpeed, exceptionDuration, maxOffset, crystalGuard,
                guardMaxOffset, clamp(value, MIN_SPEED, MAX_SPEED, DEFAULT_GUARD_SPEED));
    }

    static CameraConfig from(JsonObject json, CameraConfig fallback) {
        if (json == null) {
            return fallback;
        }
        return new CameraConfig(
                JsonValues.flag(json, "enabled", fallback.enabled),
                clamp(JsonValues.number(json, "speed", fallback.speed), MIN_SPEED, MAX_SPEED, DEFAULT_SPEED),
                clamp(JsonValues.number(json, "firstPersonSpeed", fallback.firstPersonSpeed), MIN_SPEED, MAX_SPEED,
                        DEFAULT_FIRST_PERSON_SPEED),
                (int) Math.max(MIN_DURATION, Math.min(MAX_DURATION,
                        Math.round(JsonValues.number(json, "exceptionDuration", fallback.exceptionDuration)))),
                clamp(JsonValues.number(json, "maxOffset", fallback.maxOffset), 0.0, MAX_OFFSET_LIMIT,
                        DEFAULT_MAX_OFFSET),
                JsonValues.flag(json, "crystalGuard", fallback.crystalGuard),
                clamp(JsonValues.number(json, "guardMaxOffset", fallback.guardMaxOffset), MIN_GUARD_OFFSET,
                        MAX_GUARD_OFFSET, DEFAULT_GUARD_MAX_OFFSET),
                clamp(JsonValues.number(json, "guardSpeed", fallback.guardSpeed), MIN_SPEED, MAX_SPEED,
                        DEFAULT_GUARD_SPEED));
    }

    JsonObject write() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", enabled);
        json.addProperty("speed", speed);
        json.addProperty("firstPersonSpeed", firstPersonSpeed);
        json.addProperty("exceptionDuration", exceptionDuration);
        json.addProperty("maxOffset", maxOffset);
        json.addProperty("crystalGuard", crystalGuard);
        json.addProperty("guardMaxOffset", guardMaxOffset);
        json.addProperty("guardSpeed", guardSpeed);
        return json;
    }

    private static double clamp(double value, double min, double max, double fallback) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }
}
