package dev.r3vy.motionsuite.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

final class ConfigFile {
    private static final Logger LOGGER = LoggerFactory.getLogger("motionsuite");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SCHEMA_VERSION_KEY = "schemaVersion";
    private static final int SCHEMA_VERSION = 2;
    private static final String BLUR_SECTION = "blur";
    private static final String CAMERA_SECTION = "camera";
    private static final String RENDER_SECTION = "render";
    private static final String FARM_SECTION = "farm";
    private static final String LOOK_SECTION = "freeLook";
    private static final String COMMAND_KEY_SECTION = "commandKey";
    private static final String LEGACY_TEXT_SECTION = "autoText";
    private static final String KEYS_SECTION = "keys";
    private static final String LEGACY_KEY_PREFIX = "key_";
    private static final String MOD_KEY_PREFIX = "key.motionsuite.";

    private final Path file;
    private final Path badFile;

    ConfigFile(Path file) {
        this.file = file;
        this.badFile = file.resolveSibling(file.getFileName() + ".bad");
    }

    static ConfigFile defaultLocation() {
        return new ConfigFile(FabricLoader.getInstance().getConfigDir().resolve("motionsuite.json"));
    }

    boolean exists() {
        return Files.isRegularFile(file);
    }

    ConfigSnapshot load(ConfigSnapshot fallback) {
        try {
            JsonObject root = parse();
            Map<String, String> keys = readKeys(root.get(KEYS_SECTION));
            if (keys.isEmpty()) {
                keys = importLegacyOptions();
            }
            JsonObject commandKey = section(root, COMMAND_KEY_SECTION);
            if (commandKey == null) {
                commandKey = section(root, LEGACY_TEXT_SECTION);
            }
            return new ConfigSnapshot(
                    BlurConfig.from(section(root, BLUR_SECTION), fallback.blur()),
                    CameraConfig.from(section(root, CAMERA_SECTION), fallback.camera()),
                    RenderConfig.from(section(root, RENDER_SECTION), fallback.render()),
                    FarmConfig.from(section(root, FARM_SECTION), fallback.farm()),
                    LookConfig.from(section(root, LOOK_SECTION), fallback.look()),
                    CommandKeyConfig.from(commandKey, fallback.commandKey()),
                    keys);
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Config unreadable, backed up as {} and defaults restored",
                    badFile.getFileName(), e);
            quarantine();
            return ConfigSnapshot.defaults();
        }
    }

    boolean write(ConfigSnapshot snapshot) {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(temp, GSON.toJson(serialize(snapshot)), StandardCharsets.UTF_8);
            moveIntoPlace(temp);
            return true;
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", file.getFileName(), e);
            return false;
        }
    }

    private JsonObject parse() throws IOException {
        JsonElement element = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
        if (!element.isJsonObject()) {
            throw new JsonParseException("Root is not an object");
        }
        return element.getAsJsonObject();
    }

    private JsonObject serialize(ConfigSnapshot snapshot) {
        JsonObject root = new JsonObject();
        root.addProperty(SCHEMA_VERSION_KEY, SCHEMA_VERSION);
        root.add(BLUR_SECTION, snapshot.blur().write());
        root.add(CAMERA_SECTION, snapshot.camera().write());
        root.add(RENDER_SECTION, snapshot.render().write());
        root.add(FARM_SECTION, snapshot.farm().write());
        root.add(LOOK_SECTION, snapshot.look().write());
        root.add(COMMAND_KEY_SECTION, snapshot.commandKey().write());
        JsonObject keys = new JsonObject();
        for (Map.Entry<String, String> entry : snapshot.keys().entrySet()) {
            keys.addProperty(entry.getKey(), entry.getValue());
        }
        root.add(KEYS_SECTION, keys);
        return root;
    }

    private static JsonObject section(JsonObject root, String key) {
        JsonElement element = root.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static Map<String, String> readKeys(JsonElement element) {
        Map<String, String> keys = new LinkedHashMap<>();
        if (element == null || !element.isJsonObject()) {
            return keys;
        }
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsString().startsWith("key.")) {
                keys.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return keys;
    }

    private Map<String, String> importLegacyOptions() {
        Map<String, String> keys = new LinkedHashMap<>();
        Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");
        if (!Files.isRegularFile(options)) {
            return keys;
        }
        try {
            for (String line : Files.readAllLines(options, StandardCharsets.UTF_8)) {
                int split = line.indexOf(':');
                if (split <= LEGACY_KEY_PREFIX.length() || !line.startsWith(LEGACY_KEY_PREFIX)) {
                    continue;
                }
                String id = line.substring(LEGACY_KEY_PREFIX.length(), split);
                if (id.startsWith(MOD_KEY_PREFIX)) {
                    keys.put(id, line.substring(split + 1));
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read options.txt for key migration", e);
        }
        return keys;
    }

    private void quarantine() {
        try {
            Files.move(file, badFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("Could not back up the unreadable config", e);
        }
    }

    private void moveIntoPlace(Path temp) throws IOException {
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
