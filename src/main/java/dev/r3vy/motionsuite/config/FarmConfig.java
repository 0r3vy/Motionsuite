package dev.r3vy.motionsuite.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.r3vy.motionsuite.data.FarmBlocks;
import dev.r3vy.motionsuite.data.SoundGroups;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record FarmConfig(
        int farCullRange,
        boolean blockRenderCull,
        boolean deletesTnt,
        boolean deletesItems,
        boolean hidesOrbs,
        boolean hidesThrown,
        boolean cullsFarEntities,
        boolean limitsSounds,
        Set<Block> culledBlocks,
        Set<String> mutedSounds) {
    public static final int DEFAULT_CULL_RANGE = 64;
    static final int BLOCK_LIST_VERSION = 2;
    private static final int MIN_CULL_RANGE = 0;
    private static final int MAX_CULL_RANGE = 512;

    static FarmConfig defaults() {
        FarmBlocks.ensureLoaded();
        SoundGroups.ensureLoaded();
        return new FarmConfig(DEFAULT_CULL_RANGE, false, false, false, false, false, false, false,
                FarmBlocks.all(), SoundGroups.allKeys());
    }

    public boolean freezes(Block block) {
        return blockRenderCull && culledBlocks.contains(block);
    }

    public boolean cullsBlock(Block block) {
        return culledBlocks.contains(block);
    }

    public boolean mutesSound(String key) {
        return mutedSounds.contains(key);
    }

    public FarmConfig withFarCullRange(int value) {
        return new FarmConfig(Math.max(MIN_CULL_RANGE, Math.min(MAX_CULL_RANGE, value)), blockRenderCull,
                deletesTnt, deletesItems, hidesOrbs, hidesThrown, cullsFarEntities, limitsSounds,
                culledBlocks, mutedSounds);
    }

    public FarmConfig withCullsBlocks(boolean value) {
        return new FarmConfig(farCullRange, value, deletesTnt, deletesItems, hidesOrbs, hidesThrown,
                cullsFarEntities, limitsSounds, culledBlocks, mutedSounds);
    }

    public FarmConfig withDeletesTnt(boolean value) {
        return new FarmConfig(farCullRange, blockRenderCull, value, deletesItems, hidesOrbs, hidesThrown,
                cullsFarEntities, limitsSounds, culledBlocks, mutedSounds);
    }

    public FarmConfig withDeletesItems(boolean value) {
        return new FarmConfig(farCullRange, blockRenderCull, deletesTnt, value, hidesOrbs, hidesThrown,
                cullsFarEntities, limitsSounds, culledBlocks, mutedSounds);
    }

    public FarmConfig withHidesOrbs(boolean value) {
        return new FarmConfig(farCullRange, blockRenderCull, deletesTnt, deletesItems, value, hidesThrown,
                cullsFarEntities, limitsSounds, culledBlocks, mutedSounds);
    }

    public FarmConfig withHidesThrown(boolean value) {
        return new FarmConfig(farCullRange, blockRenderCull, deletesTnt, deletesItems, hidesOrbs, value,
                cullsFarEntities, limitsSounds, culledBlocks, mutedSounds);
    }

    public FarmConfig withCullsFarEntities(boolean value) {
        return new FarmConfig(farCullRange, blockRenderCull, deletesTnt, deletesItems, hidesOrbs, hidesThrown,
                value, limitsSounds, culledBlocks, mutedSounds);
    }

    public FarmConfig withLimitsSounds(boolean value) {
        return new FarmConfig(farCullRange, blockRenderCull, deletesTnt, deletesItems, hidesOrbs, hidesThrown,
                cullsFarEntities, value, culledBlocks, mutedSounds);
    }

    public FarmConfig withBlockCulled(Block block, boolean value) {
        Set<Block> next = new LinkedHashSet<>(culledBlocks);
        if (value) {
            next.add(block);
        } else {
            next.remove(block);
        }
        return new FarmConfig(farCullRange, blockRenderCull, deletesTnt, deletesItems, hidesOrbs, hidesThrown,
                cullsFarEntities, limitsSounds, Set.copyOf(next), mutedSounds);
    }

    public FarmConfig withAllBlocksCulled(boolean value) {
        Set<Block> next = value ? FarmBlocks.all() : Set.of();
        return new FarmConfig(farCullRange, blockRenderCull, deletesTnt, deletesItems, hidesOrbs, hidesThrown,
                cullsFarEntities, limitsSounds, next, mutedSounds);
    }

    public FarmConfig withSoundMuted(String key, boolean value) {
        Set<String> next = new LinkedHashSet<>(mutedSounds);
        if (value) {
            next.add(key);
        } else {
            next.remove(key);
        }
        return new FarmConfig(farCullRange, blockRenderCull, deletesTnt, deletesItems, hidesOrbs, hidesThrown,
                cullsFarEntities, limitsSounds, culledBlocks, Set.copyOf(next));
    }

    public FarmConfig withAllSoundsMuted(boolean value) {
        Set<String> next = value ? SoundGroups.allKeys() : Set.of();
        return new FarmConfig(farCullRange, blockRenderCull, deletesTnt, deletesItems, hidesOrbs, hidesThrown,
                cullsFarEntities, limitsSounds, culledBlocks, next);
    }

    static FarmConfig from(JsonObject json, FarmConfig fallback) {
        if (json == null) {
            return fallback;
        }
        Set<String> muted = readMuted(json, fallback);
        Set<Block> blocks = readBlocks(json, fallback);
        return new FarmConfig(
                (int) Math.max(MIN_CULL_RANGE, Math.min(MAX_CULL_RANGE,
                        Math.round(JsonValues.number(json, "farCullRange", fallback.farCullRange)))),
                JsonValues.flag(json, "blockRenderCull", legacyCull(json) || fallback.blockRenderCull),
                JsonValues.flag(json, "deleteTnt", fallback.deletesTnt),
                JsonValues.flag(json, "deleteItems", fallback.deletesItems),
                JsonValues.flag(json, "hideOrbs", fallback.hidesOrbs),
                JsonValues.flag(json, "hideThrown", fallback.hidesThrown),
                JsonValues.flag(json, "farCull", fallback.cullsFarEntities),
                JsonValues.flag(json, "limitSounds", fallback.limitsSounds),
                blocks,
                muted);
    }

    JsonObject write() {
        JsonObject json = new JsonObject();
        json.addProperty("farCullRange", farCullRange);
        json.addProperty("blockRenderCull", blockRenderCull);
        json.addProperty("deleteTnt", deletesTnt);
        json.addProperty("deleteItems", deletesItems);
        json.addProperty("hideOrbs", hidesOrbs);
        json.addProperty("hideThrown", hidesThrown);
        json.addProperty("farCull", cullsFarEntities);
        json.addProperty("limitSounds", limitsSounds);
        JsonArray blocks = new JsonArray();
        for (Block block : FarmBlocks.allInOrder()) {
            if (culledBlocks.contains(block)) {
                blocks.add(Registries.BLOCK.getId(block).toString());
            }
        }
        json.add("culledBlocks", blocks);
        json.addProperty("blockListVersion", BLOCK_LIST_VERSION);
        JsonArray sounds = new JsonArray();
        for (String key : SoundGroups.sortedKeys(mutedSounds)) {
            sounds.add(key);
        }
        json.add("mutedSounds", sounds);
        return json;
    }

    private static Set<String> readMuted(JsonObject json, FarmConfig fallback) {
        JsonElement element = json.get("mutedSounds");
        if (element == null || !element.isJsonArray()) {
            return fallback.mutedSounds;
        }
        Set<String> parsed = new LinkedHashSet<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            if (entry.isJsonPrimitive() && SoundGroups.byKey(entry.getAsString()) != null) {
                parsed.add(entry.getAsString());
            }
        }
        return Set.copyOf(parsed);
    }

    private static Set<Block> readBlocks(JsonObject json, FarmConfig fallback) {
        JsonElement stored = json.get("culledBlocks");
        JsonElement version = json.get("blockListVersion");
        int listVersion = version != null && version.isJsonPrimitive() && version.getAsJsonPrimitive().isNumber()
                ? version.getAsInt() : 0;
        if (listVersion < BLOCK_LIST_VERSION || stored == null || !stored.isJsonArray()) {
            return fallback.culledBlocks;
        }
        Set<Block> parsed = new LinkedHashSet<>();
        for (JsonElement entry : stored.getAsJsonArray()) {
            Block block = entry.isJsonPrimitive() ? blockById(entry.getAsString()) : null;
            if (block != null) {
                parsed.add(block);
            }
        }
        if (parsed.isEmpty() && !stored.getAsJsonArray().isEmpty()) {
            return fallback.culledBlocks;
        }
        return Set.copyOf(parsed);
    }

    private static Block blockById(String value) {
        try {
            return Registries.BLOCK.get(Identifier.of(value));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean legacyCull(JsonObject json) {
        return JsonValues.flag(json, "hideRedstone", false) || JsonValues.flag(json, "hideObserver", false)
                || JsonValues.flag(json, "hidePistons", false) || JsonValues.flag(json, "hideFurnace", false)
                || JsonValues.flag(json, "hideWither", false);
    }

    public List<Block> farmBlocksView() {
        return FarmBlocks.allInOrder();
    }
}
