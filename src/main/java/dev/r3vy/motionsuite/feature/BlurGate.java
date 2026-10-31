package dev.r3vy.motionsuite.feature;

import dev.r3vy.motionsuite.config.ConfigSnapshot;
import dev.r3vy.motionsuite.ui.HubScreen;
import net.minecraft.client.MinecraftClient;

public final class BlurGate {
    private BlurGate() {
    }

    public static boolean shouldRun(ConfigSnapshot config, MinecraftClient client, boolean idle) {
        return blurWanted(config, client, idle) && (inWorld(client) || hubOpen(client));
    }

    public static boolean shouldRunOnVulkan(ConfigSnapshot config, MinecraftClient client, boolean idle) {
        return blurWanted(config, client, idle) && inWorld(client);
    }

    public static boolean inWorld(MinecraftClient client) {
        return client.world != null && client.player != null && client.currentScreen == null;
    }

    private static boolean hubOpen(MinecraftClient client) {
        return client.world != null && client.player != null && client.currentScreen instanceof HubScreen;
    }

    private static boolean blurWanted(ConfigSnapshot config, MinecraftClient client, boolean idle) {
        return config.blur().amount() > 0
                && client.isWindowFocused()
                && !idle;
    }
}
