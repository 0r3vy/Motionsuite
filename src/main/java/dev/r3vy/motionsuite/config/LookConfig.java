package dev.r3vy.motionsuite.config;

import com.google.gson.JsonObject;

public record LookConfig(boolean enabled, boolean toggleMode, boolean thirdPerson) {
    static LookConfig defaults() {
        return new LookConfig(true, false, false);
    }

    public LookConfig withEnabled(boolean value) {
        return new LookConfig(value, toggleMode, thirdPerson);
    }

    public LookConfig withToggleMode(boolean value) {
        return new LookConfig(enabled, value, thirdPerson);
    }

    public LookConfig withThirdPerson(boolean value) {
        return new LookConfig(enabled, toggleMode, value);
    }

    static LookConfig from(JsonObject json, LookConfig fallback) {
        if (json == null) {
            return fallback;
        }
        return new LookConfig(
                JsonValues.flag(json, "enabled", fallback.enabled),
                JsonValues.flag(json, "toggleMode", fallback.toggleMode),
                JsonValues.flag(json, "thirdPerson", fallback.thirdPerson));
    }

    JsonObject write() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", enabled);
        json.addProperty("toggleMode", toggleMode);
        json.addProperty("thirdPerson", thirdPerson);
        return json;
    }
}
