package dev.r3vy.motionsuite.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public final class HubKey {
    private final KeyBinding binding;

    public HubKey(KeyBinding binding) {
        this.binding = binding;
    }

    public KeyBinding binding() {
        return binding;
    }

    public void poll(MinecraftClient client) {
        while (binding.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new HubScreen(null));
            }
        }
    }
}
