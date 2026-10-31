package dev.r3vy.motionsuite.feature;

import dev.r3vy.motionsuite.config.ConfigSnapshot;
import dev.r3vy.motionsuite.config.RenderToggle;
import net.minecraft.client.MinecraftClient;

public final class HudCleaner {
    public void tick(MinecraftClient client, ConfigSnapshot config) {
        if (client.inGameHud != null && config.render().hides(RenderToggle.TITLE)) {
            client.inGameHud.clearTitle();
        }
    }
}
