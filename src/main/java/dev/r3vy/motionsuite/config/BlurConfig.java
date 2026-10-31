package dev.r3vy.motionsuite.config;

import com.google.gson.JsonObject;

public record BlurConfig(int amount, boolean adaptive, int guardCut, boolean smart, int stillBlur) {
    static final int DEFAULT_AMOUNT = 50;
    static final int DEFAULT_GUARD_CUT = 0;
    static final int DEFAULT_STILL_BLUR = 35;
    private static final int MAX_PERCENT = 100;
    private static final int MAX_BLEND_PERCENT = 99;

    static BlurConfig defaults() {
        return new BlurConfig(DEFAULT_AMOUNT, false, DEFAULT_GUARD_CUT, false, DEFAULT_STILL_BLUR);
    }

    public float blend() {
        return Math.min(amount, MAX_BLEND_PERCENT) / 100.0f;
    }

    public BlurConfig withAmount(int value) {
        return new BlurConfig(clampPercent(value), adaptive, guardCut, smart, stillBlur);
    }

    public BlurConfig withAdaptive(boolean value) {
        return new BlurConfig(amount, value, guardCut, smart, stillBlur);
    }

    public BlurConfig withGuardCut(int value) {
        return new BlurConfig(amount, adaptive, clampPercent(value), smart, stillBlur);
    }

    public BlurConfig withSmart(boolean value) {
        return new BlurConfig(amount, adaptive, guardCut, value, stillBlur);
    }

    public BlurConfig withStillBlur(int value) {
        return new BlurConfig(amount, adaptive, guardCut, smart, clampPercent(value));
    }

    static BlurConfig from(JsonObject json, BlurConfig fallback) {
        if (json == null) {
            return fallback;
        }
        return new BlurConfig(
                clampPercent(Math.round(JsonValues.number(json, "amount", fallback.amount))),
                JsonValues.flag(json, "adaptive", fallback.adaptive),
                clampPercent(Math.round(JsonValues.number(json, "guardCut", fallback.guardCut))),
                JsonValues.flag(json, "smart", fallback.smart),
                clampPercent(Math.round(JsonValues.number(json, "stillBlur", fallback.stillBlur))));
    }

    JsonObject write() {
        JsonObject json = new JsonObject();
        json.addProperty("amount", amount);
        json.addProperty("adaptive", adaptive);
        json.addProperty("guardCut", guardCut);
        json.addProperty("smart", smart);
        json.addProperty("stillBlur", stillBlur);
        return json;
    }

    private static int clampPercent(long value) {
        return (int) Math.max(0L, Math.min(MAX_PERCENT, value));
    }
}
