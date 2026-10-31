package dev.r3vy.motionsuite.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class JsonValues {
    private JsonValues() {
    }

    static double number(JsonObject json, String key, double fallback) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }
        double value = element.getAsDouble();
        return Double.isNaN(value) || Double.isInfinite(value) ? fallback : value;
    }

    static boolean flag(JsonObject json, String key, boolean fallback) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            return fallback;
        }
        return element.getAsBoolean();
    }

    static String string(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                ? element.getAsString() : null;
    }
}
