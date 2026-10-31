package dev.r3vy.motionsuite.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class FarmBlocks {
    private static final Logger LOGGER = LoggerFactory.getLogger("motionsuite");
    private static final String DATA_PATH = "assets/motionsuite/data/farm_blocks.json";

    private static volatile Set<Block> blocks = Set.of();
    private static volatile List<Block> ordered = List.of();

    private FarmBlocks() {
    }

    public static void ensureLoaded() {
        if (blocks.isEmpty()) {
            load();
        }
    }

    static void load() {
        try (InputStream stream = FarmBlocks.class.getClassLoader().getResourceAsStream(DATA_PATH)) {
            if (stream == null) {
                LOGGER.error("Missing farm block data {}", DATA_PATH);
                return;
            }
            JsonObject root = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            Set<Block> parsed = new LinkedHashSet<>();
            JsonElement explicit = root.get("blocks");
            if (explicit != null && explicit.isJsonArray()) {
                for (JsonElement entry : explicit.getAsJsonArray()) {
                    if (entry.isJsonPrimitive()) {
                        Block block = byId(entry.getAsString());
                        if (block != null) {
                            parsed.add(block);
                        }
                    }
                }
            }
            if (flag(root, "doors")) {
                for (Block block : Registries.BLOCK) {
                    if (block instanceof DoorBlock) {
                        parsed.add(block);
                    }
                }
            }
            if (flag(root, "trapdoors")) {
                for (Block block : Registries.BLOCK) {
                    if (block instanceof TrapdoorBlock) {
                        parsed.add(block);
                    }
                }
            }
            if (parsed.isEmpty()) {
                LOGGER.error("Farm block data {} is empty", DATA_PATH);
                return;
            }
            ordered = List.copyOf(parsed);
            blocks = Set.copyOf(parsed);
        } catch (Exception e) {
            LOGGER.error("Could not load farm block data {}", DATA_PATH, e);
        }
    }

    public static Set<Block> all() {
        return blocks;
    }

    public static List<Block> allInOrder() {
        return ordered;
    }

    private static boolean flag(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsBoolean();
    }

    private static Block byId(String value) {
        try {
            return Registries.BLOCK.get(Identifier.of(value));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
