package dev.r3vy.motionsuite.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public record CommandKeyConfig(
        boolean enabled,
        int cooldownMs,
        int slotCount,
        boolean perServer,
        List<String> messages,
        Map<String, List<String>> servers) {
    public static final int DEFAULT_SLOTS = 1;
    public static final int MAX_SLOTS = 4;
    public static final int MAX_LENGTH = 256;
    static final int DEFAULT_COOLDOWN = 1500;
    public static final int MIN_COOLDOWN = 250;
    public static final int MAX_COOLDOWN = 10000;

    public CommandKeyConfig {
        messages = normalize(messages);
        servers = Map.copyOf(servers);
    }

    static CommandKeyConfig defaults() {
        return new CommandKeyConfig(true, DEFAULT_COOLDOWN, DEFAULT_SLOTS, false,
                blankMessages(), Map.of());
    }

    public String message(int slot) {
        return messages.get(slot);
    }

    public boolean hasServer(String address) {
        return address != null && servers.containsKey(normalize(address));
    }

    public String serverMessage(String address, int slot) {
        List<String> profile = address == null ? null : servers.get(normalize(address));
        return profile == null ? null : profile.get(slot);
    }

    public String activeMessage(String address, int slot) {
        List<String> profile = perServer && address != null ? servers.get(normalize(address)) : null;
        return profile != null ? profile.get(slot) : messages.get(slot);
    }

    public List<String> serverAddresses() {
        List<String> list = new ArrayList<>(servers.keySet());
        Collections.sort(list);
        return list;
    }

    public CommandKeyConfig withEnabled(boolean value) {
        return new CommandKeyConfig(value, cooldownMs, slotCount, perServer, messages, servers);
    }

    public CommandKeyConfig withCooldownMs(int value) {
        return new CommandKeyConfig(enabled, Math.max(MIN_COOLDOWN, Math.min(MAX_COOLDOWN, value)),
                slotCount, perServer, messages, servers);
    }

    public CommandKeyConfig withSlotCount(int value) {
        int clamped = Math.max(1, Math.min(MAX_SLOTS, value));
        return new CommandKeyConfig(enabled, cooldownMs, clamped, perServer, messages, servers);
    }

    public CommandKeyConfig withPerServer(boolean value) {
        return new CommandKeyConfig(enabled, cooldownMs, slotCount, value, messages, servers);
    }

    public CommandKeyConfig withMessage(int slot, String value) {
        List<String> next = new ArrayList<>(messages);
        next.set(slot, trim(value));
        return new CommandKeyConfig(enabled, cooldownMs, slotCount, perServer, next, servers);
    }

    public CommandKeyConfig withServerMessage(String address, int slot, String value) {
        if (address == null) {
            return withMessage(slot, value);
        }
        String key = normalize(address);
        List<String> profile = new ArrayList<>(servers.getOrDefault(key, messages));
        profile.set(slot, trim(value));
        Map<String, List<String>> next = new LinkedHashMap<>(servers);
        next.put(key, profile);
        return new CommandKeyConfig(enabled, cooldownMs, slotCount, perServer, messages, next);
    }

    public CommandKeyConfig withServerAdded(String address) {
        if (address == null) {
            return this;
        }
        String key = normalize(address);
        if (servers.containsKey(key)) {
            return this;
        }
        Map<String, List<String>> next = new LinkedHashMap<>(servers);
        next.put(key, new ArrayList<>(messages));
        return new CommandKeyConfig(enabled, cooldownMs, slotCount, perServer, messages, next);
    }

    public CommandKeyConfig withServerForgotten(String address) {
        if (address == null) {
            return this;
        }
        Map<String, List<String>> next = new LinkedHashMap<>(servers);
        next.remove(normalize(address));
        return new CommandKeyConfig(enabled, cooldownMs, slotCount, perServer, messages, next);
    }

    static CommandKeyConfig from(JsonObject json, CommandKeyConfig fallback) {
        if (json == null) {
            return fallback;
        }
        CommandKeyConfig parsed = new CommandKeyConfig(
                JsonValues.flag(json, "enabled", fallback.enabled),
                (int) Math.round(JsonValues.number(json, "cooldownMs", fallback.cooldownMs)),
                (int) Math.round(JsonValues.number(json, "slotCount", fallback.slotCount)),
                JsonValues.flag(json, "perServer", fallback.perServer),
                readMessages(json.get("messages"), fallback),
                readServers(json.get("servers"), fallback));
        int used = usedSlots(parsed.messages);
        return parsed.withCooldownMs(parsed.cooldownMs)
                .withSlotCount(Math.max(parsed.slotCount, used));
    }

    private static int usedSlots(List<String> messages) {
        int used = 1;
        for (int slot = 0; slot < messages.size(); slot++) {
            if (!messages.get(slot).isBlank()) {
                used = slot + 1;
            }
        }
        return used;
    }

    JsonObject write() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", enabled);
        json.addProperty("cooldownMs", cooldownMs);
        json.addProperty("slotCount", slotCount);
        json.addProperty("perServer", perServer);
        JsonArray array = new JsonArray();
        for (String message : messages) {
            array.add(message);
        }
        json.add("messages", array);
        JsonObject map = new JsonObject();
        for (Map.Entry<String, List<String>> entry : new TreeMap<>(servers).entrySet()) {
            JsonArray profile = new JsonArray();
            for (String message : entry.getValue()) {
                profile.add(message);
            }
            map.add(entry.getKey(), profile);
        }
        json.add("servers", map);
        return json;
    }

    private static List<String> readMessages(JsonElement list, CommandKeyConfig fallback) {
        if (list == null || !list.isJsonArray()) {
            return fallback.messages;
        }
        JsonArray array = list.getAsJsonArray();
        List<String> parsed = new ArrayList<>(blankMessages());
        for (int slot = 0; slot < MAX_SLOTS && slot < array.size(); slot++) {
            JsonElement entry = array.get(slot);
            if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
                parsed.set(slot, trim(entry.getAsString()));
            }
        }
        return parsed;
    }

    private static Map<String, List<String>> readServers(JsonElement map, CommandKeyConfig fallback) {
        if (map == null || !map.isJsonObject()) {
            return fallback.servers;
        }
        Map<String, List<String>> parsed = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : map.getAsJsonObject().entrySet()) {
            if (!entry.getValue().isJsonArray()) {
                continue;
            }
            List<String> profile = new ArrayList<>(blankMessages());
            JsonArray array = entry.getValue().getAsJsonArray();
            for (int slot = 0; slot < MAX_SLOTS && slot < array.size(); slot++) {
                JsonElement item = array.get(slot);
                if (item.isJsonPrimitive() && item.getAsJsonPrimitive().isString()) {
                    profile.set(slot, trim(item.getAsString()));
                }
            }
            parsed.put(normalize(entry.getKey()), profile);
        }
        return parsed;
    }

    private static List<String> normalize(List<String> messages) {
        List<String> fixed = new ArrayList<>(MAX_SLOTS);
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            fixed.add(slot < messages.size() ? trim(messages.get(slot)) : "");
        }
        return List.copyOf(fixed);
    }

    private static List<String> blankMessages() {
        return List.of("", "", "", "");
    }

    private static String normalize(String address) {
        return address.strip().toLowerCase(Locale.ROOT);
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > MAX_LENGTH ? value.substring(0, MAX_LENGTH) : value;
    }
}
