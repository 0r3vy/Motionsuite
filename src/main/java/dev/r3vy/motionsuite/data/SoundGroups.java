package dev.r3vy.motionsuite.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SoundGroups {
    private static final Logger LOGGER = LoggerFactory.getLogger("motionsuite");
    private static final String DATA_PATH = "assets/motionsuite/data/sound_groups.json";

    private static volatile List<Group> groups = List.of();

    private SoundGroups() {
    }

    public static void ensureLoaded() {
        if (groups.isEmpty()) {
            load();
        }
    }

    static void load() {
        try (InputStream stream = SoundGroups.class.getClassLoader().getResourceAsStream(DATA_PATH)) {
            if (stream == null) {
                LOGGER.error("Missing sound group data {}", DATA_PATH);
                return;
            }
            JsonObject root = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            JsonArray array = root.getAsJsonArray("groups");
            Map<String, Group> parsed = new LinkedHashMap<>();
            for (JsonElement element : array) {
                Group group = parseGroup(element.getAsJsonObject());
                if (group != null) {
                    parsed.put(group.key(), group);
                }
            }
            if (parsed.isEmpty()) {
                LOGGER.error("Sound group data {} is empty", DATA_PATH);
                return;
            }
            groups = List.copyOf(parsed.values());
        } catch (Exception e) {
            LOGGER.error("Could not load sound group data {}", DATA_PATH, e);
        }
    }

    public static List<Group> all() {
        return groups;
    }

    public static Set<String> allKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for (Group group : groups) {
            keys.add(group.key());
        }
        return Set.copyOf(keys);
    }

    public static List<String> sortedKeys(Collection<String> subset) {
        List<String> keys = new ArrayList<>();
        for (Group group : groups) {
            if (subset.contains(group.key())) {
                keys.add(group.key());
            }
        }
        return List.copyOf(keys);
    }

    public static Group byKey(String key) {
        for (Group group : groups) {
            if (group.key().equals(key)) {
                return group;
            }
        }
        return null;
    }

    public static String matchMutedKey(Set<String> muted, String path) {
        for (Group group : groups) {
            if (muted.contains(group.key()) && group.matches(path)) {
                return group.key();
            }
        }
        return null;
    }

    private static Group parseGroup(JsonObject json) {
        String key = json.has("key") && json.get("key").isJsonPrimitive() ? json.get("key").getAsString() : null;
        if (key == null || key.isBlank()) {
            return null;
        }
        return new Group(key, readStrings(json, "exact"), readStrings(json, "prefixes"),
                readStrings(json, "suffixes"));
    }

    private static Set<String> readStrings(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonArray()) {
            return Set.of();
        }
        Set<String> values = new LinkedHashSet<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            if (entry.isJsonPrimitive()) {
                values.add(entry.getAsString());
            }
        }
        return Set.copyOf(values);
    }

    public record Group(String key, Set<String> exact, Set<String> prefixes, Set<String> suffixes) {
        public Group {
            exact = Set.copyOf(exact);
            prefixes = Set.copyOf(prefixes);
            suffixes = Set.copyOf(suffixes);
        }

        public boolean matches(String path) {
            if (exact.contains(path)) {
                return true;
            }
            for (String prefix : prefixes) {
                if (path.startsWith(prefix)) {
                    return true;
                }
            }
            for (String suffix : suffixes) {
                if (path.endsWith(suffix)) {
                    return true;
                }
            }
            return false;
        }
    }
}
