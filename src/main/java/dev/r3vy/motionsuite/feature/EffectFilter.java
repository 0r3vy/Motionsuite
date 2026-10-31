package dev.r3vy.motionsuite.feature;

import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.ConfigManager;
import dev.r3vy.motionsuite.config.ConfigSnapshot;
import dev.r3vy.motionsuite.config.RenderToggle;
import dev.r3vy.motionsuite.data.SoundGroups;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;

import java.util.Set;

public final class EffectFilter {
    private static final Set<Identifier> TNT_SOUNDS = Set.of(
            Identifier.ofVanilla("entity.tnt.primed"),
            Identifier.ofVanilla("entity.generic.explode"));

    private static final Set<Identifier> THROWN_SOUNDS = Set.of(
            Identifier.ofVanilla("entity.experience_bottle.throw"),
            Identifier.ofVanilla("entity.egg.throw"),
            Identifier.ofVanilla("entity.snowball.throw"),
            Identifier.ofVanilla("entity.splash_potion.throw"),
            Identifier.ofVanilla("entity.lingering_potion.throw"),
            Identifier.ofVanilla("entity.ender_pearl.throw"));

    private static final Set<Identifier> PISTON_SOUNDS = Set.of(
            Identifier.ofVanilla("block.piston.extend"),
            Identifier.ofVanilla("block.piston.contract"));

    private static final Set<Identifier> WEATHER_SOUNDS = Set.of(
            Identifier.ofVanilla("weather.rain"),
            Identifier.ofVanilla("weather.rain.above"),
            Identifier.ofVanilla("entity.lightning_bolt.thunder"),
            Identifier.ofVanilla("entity.lightning_bolt.impact"));

    private static final Identifier ORB_PICKUP = Identifier.ofVanilla("entity.experience_orb.pickup");
    private static final Identifier ITEM_PICKUP = Identifier.ofVanilla("entity.item.pickup");
    private static final Identifier LEVEL_UP = Identifier.ofVanilla("entity.player.levelup");
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final String PLAYER_PREFIX = "entity.player.";

    private final Object2ObjectOpenHashMap<Identifier, Boolean> soundCache = new Object2ObjectOpenHashMap<>();
    private long cachedEpoch = -1L;

    public boolean allowSound(Identifier id, ConfigSnapshot config) {
        if (config.render().hides(RenderToggle.WEATHER) && WEATHER_SOUNDS.contains(id)) {
            return false;
        }
        if (config.farm().deletesTnt() && TNT_SOUNDS.contains(id)) {
            return false;
        }
        if (config.farm().hidesThrown() && THROWN_SOUNDS.contains(id)) {
            return false;
        }
        if (config.farm().hidesOrbs() && ORB_PICKUP.equals(id)) {
            return false;
        }
        if (config.farm().deletesItems() && ITEM_PICKUP.equals(id)) {
            return false;
        }
        if (config.farm().blockRenderCull() && PISTON_SOUNDS.contains(id)) {
            return false;
        }
        if (!config.farm().limitsSounds()) {
            return true;
        }
        return !isLagSound(id, config);
    }

    public synchronized boolean allowSoundCached(Identifier id) {
        ConfigManager.FilterState state = Runtime.configs().filterState();
        if (state.epoch() != cachedEpoch) {
            soundCache.clear();
            cachedEpoch = state.epoch();
        }
        Boolean cached = soundCache.get(id);
        if (cached != null) {
            return cached;
        }
        boolean allowed = allowSound(id, state.snapshot());
        soundCache.put(id, allowed);
        return allowed;
    }

    public boolean allowParticle(ParticleType<?> type, ConfigSnapshot config) {
        return !config.farm().deletesTnt() || !isBlastParticle(type);
    }

    private boolean isLagSound(Identifier id, ConfigSnapshot config) {
        if (!id.getNamespace().equals(MINECRAFT_NAMESPACE)) {
            return false;
        }
        String path = id.getPath();
        boolean playerSound = path.startsWith(PLAYER_PREFIX);
        if (playerSound && !LEVEL_UP.equals(id)) {
            return false;
        }
        return SoundGroups.matchMutedKey(config.farm().mutedSounds(), path) != null;
    }

    private static boolean isBlastParticle(ParticleType<?> type) {
        return type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER
                || type == ParticleTypes.SMOKE || type == ParticleTypes.LARGE_SMOKE
                || type == ParticleTypes.POOF;
    }
}
