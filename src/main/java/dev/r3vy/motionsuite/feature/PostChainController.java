package dev.r3vy.motionsuite.feature;

import dev.r3vy.motionsuite.PostEffectControl;
import dev.r3vy.motionsuite.config.ConfigSnapshot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;

public final class PostChainController {
    private static final Identifier CHAIN_ID = Identifier.of("motionsuite", "motion_blur");

    private final MinecraftClient client;

    public PostChainController(MinecraftClient client) {
        this.client = client;
    }

    public void tick(ConfigSnapshot config, boolean shouldRun, FrameBlend blend) {
        GameRenderer renderer = client.gameRenderer;
        if (!(renderer instanceof PostEffectControl control)) {
            return;
        }
        Identifier current = control.motionsuite$getEffectId();

        if (!shouldRun) {
            if (CHAIN_ID.equals(current)) {
                control.motionsuite$setEffectActive(false);
                control.motionsuite$setEffectId(null);
            }
            blend.reset();
            return;
        }
        if (current != null && !CHAIN_ID.equals(current)) {
            return;
        }
        if (current == null) {
            control.motionsuite$setEffectId(CHAIN_ID);
        }
        control.motionsuite$setEffectActive(true);
    }
}
