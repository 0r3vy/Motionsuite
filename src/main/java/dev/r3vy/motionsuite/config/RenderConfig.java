package dev.r3vy.motionsuite.config;

import com.google.gson.JsonObject;

import java.util.EnumSet;
import java.util.Set;

public record RenderConfig(Set<RenderToggle> active, double fullbright, FullbrightMode mode) {
    static final double DEFAULT_FULLBRIGHT = 0.7;
    static final float MAX_GAMMA_BOOST = 16.0f;

    static RenderConfig defaults() {
        return new RenderConfig(EnumSet.noneOf(RenderToggle.class), DEFAULT_FULLBRIGHT, FullbrightMode.NIGHT_VISION);
    }

    public boolean hides(RenderToggle toggle) {
        return active.contains(toggle);
    }

    public boolean claimsNightVision() {
        return hides(RenderToggle.FULLBRIGHT) && mode == FullbrightMode.NIGHT_VISION;
    }

    public float gammaBoost(float vanilla) {
        if (!hides(RenderToggle.FULLBRIGHT) || mode != FullbrightMode.GAMMA) {
            return vanilla;
        }
        float boosted = (float) fullbright * MAX_GAMMA_BOOST;
        return Math.max(vanilla, boosted);
    }

    public RenderConfig with(RenderToggle toggle, boolean value) {
        EnumSet<RenderToggle> next = EnumSet.copyOf(active);
        if (value) {
            next.add(toggle);
        } else {
            next.remove(toggle);
        }
        return new RenderConfig(next, fullbright, mode);
    }

    public RenderConfig withFullbright(double value) {
        return new RenderConfig(active, Math.max(0.0, Math.min(1.0, value)), mode);
    }

    public RenderConfig withMode(FullbrightMode value) {
        return new RenderConfig(active, fullbright, value == null ? FullbrightMode.NIGHT_VISION : value);
    }

    static RenderConfig from(JsonObject json, RenderConfig fallback) {
        if (json == null) {
            return fallback;
        }
        RenderConfig parsed = fallback;
        for (RenderToggle toggle : RenderToggle.values()) {
            parsed = parsed.with(toggle, JsonValues.flag(json, toggle.key(), fallback.hides(toggle)));
        }
        parsed = parsed.withFullbright(JsonValues.number(json, "fullbrightStrength", fallback.fullbright));
        parsed = parsed.withMode(FullbrightMode.byId(JsonValues.string(json, "mode"), fallback.mode));
        return parsed;
    }

    JsonObject write() {
        JsonObject json = new JsonObject();
        for (RenderToggle toggle : RenderToggle.values()) {
            json.addProperty(toggle.key(), hides(toggle));
        }
        json.addProperty("fullbrightStrength", fullbright);
        json.addProperty("mode", mode.id());
        return json;
    }
}
