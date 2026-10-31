package dev.r3vy.motionsuite.commandkey;

import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.CommandKeyConfig;
import dev.r3vy.motionsuite.config.ConfigSnapshot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Util;

import java.util.Locale;

public final class CommandKeys {
    private static final long NEVER = Long.MIN_VALUE / 2;

    private final KeyBinding[] bindings;
    private long lastSentAt = NEVER;

    public CommandKeys(MinecraftClient client, KeyBinding[] bindings) {
        this.bindings = bindings;
    }

    public KeyBinding binding(int slot) {
        return bindings[slot];
    }

    public static String serverAddress(MinecraftClient client) {
        ServerInfo server = client.getCurrentServerEntry();
        return server == null ? null : server.address.toLowerCase(Locale.ROOT);
    }

    public void tick(MinecraftClient client, ConfigSnapshot config) {
        CommandKeyConfig key = config.commandKey();
        boolean usable = key.enabled()
                && client.player != null
                && client.world != null
                && client.currentScreen == null;
        for (int slot = 0; slot < key.slotCount(); slot++) {
            KeyBinding binding = slot < bindings.length ? bindings[slot] : null;
            if (binding == null) {
                continue;
            }
            while (binding.wasPressed()) {
                if (usable) {
                    send(client, key, slot);
                }
            }
        }
    }

    private void send(MinecraftClient client, CommandKeyConfig key, int slot) {
        String message = key.activeMessage(serverAddress(client), slot).strip();
        if (message.isEmpty()) {
            return;
        }
        long now = Util.getMeasuringTimeMs();
        if (now - lastSentAt < key.cooldownMs()) {
            return;
        }
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null) {
            return;
        }
        if (message.charAt(0) == '/') {
            String command = message.substring(1).strip();
            if (command.isEmpty()) {
                return;
            }
            handler.sendChatCommand(command);
        } else {
            handler.sendChatMessage(message);
        }
        lastSentAt = now;
    }
}
