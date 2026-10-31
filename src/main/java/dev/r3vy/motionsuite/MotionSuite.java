package dev.r3vy.motionsuite;

import dev.r3vy.motionsuite.command.Commands;
import dev.r3vy.motionsuite.commandkey.CommandKeys;
import dev.r3vy.motionsuite.config.ConfigSnapshot;
import dev.r3vy.motionsuite.data.FarmBlocks;
import dev.r3vy.motionsuite.data.SoundGroups;
import dev.r3vy.motionsuite.feature.BlurGate;
import dev.r3vy.motionsuite.feature.FreeLook;
import dev.r3vy.motionsuite.input.KeyBindings;
import dev.r3vy.motionsuite.ui.HubKey;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

public final class MotionSuite implements ClientModInitializer {
    private CommandKeys commandKeys;
    private FreeLook freeLook;
    private VulkanBridge vulkan;
    private HubKey hubKey;

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        SoundGroups.ensureLoaded();
        FarmBlocks.ensureLoaded();

        KeyBinding lookKey = KeyBindings.register("key.motionsuite.freelook", GLFW.GLFW_KEY_UNKNOWN);
        KeyBinding hubKeyBinding = KeyBindings.register("key.motionsuite.hub", GLFW.GLFW_KEY_M);
        KeyBinding[] commandSlots = new KeyBinding[dev.r3vy.motionsuite.config.CommandKeyConfig.MAX_SLOTS];
        for (int slot = 0; slot < commandSlots.length; slot++) {
            commandSlots[slot] = KeyBindings.register("key.motionsuite.commandkey." + (slot + 1),
                    GLFW.GLFW_KEY_UNKNOWN);
        }

        freeLook = new FreeLook(client, lookKey);
        commandKeys = new CommandKeys(client, commandSlots);
        vulkan = new VulkanBridge();
        hubKey = new HubKey(hubKeyBinding);
        Runtime.init(client, freeLook, vulkan);
        Runtime.configs().load();
        KeyBindings.applyStored(Runtime.config().keys());

        Commands.register();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> Runtime.glide().onEntityUnload(entity));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, disconnected) -> {
            Runtime.churn().clear();
            Runtime.faults().resetAll();
            Runtime.vulkan().disconnect();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(cl -> {
            Runtime.configs().updateQuiet(s -> s.withKeys(KeyBindings.capture()));
            Runtime.configs().flushNow();
        });
    }

    private void onEntityLoad(Entity entity, ClientWorld world) {
        if (Runtime.budget().discardsOnLoad(entity, Runtime.config())) {
            entity.discard();
            return;
        }
        Runtime.glide().onEntityLoad(entity);
    }

    private void onEndTick(MinecraftClient client) {
        ConfigSnapshot config = Runtime.config();
        Runtime.churn().tick(client, config);
        freeLook.tick(config, Runtime.blend());
        commandKeys.tick(client, config);
        Runtime.hud().tick(client, config);
        Runtime.glide().tick(config, Runtime.blend());
        Runtime.tracker().observe(client, freeLook, Runtime.blend(), Runtime.glide().isSmoothing());
        hubKey.poll(client);
        if (Runtime.vulkan().present()) {
            Runtime.vulkan().tick();
        } else {
            Runtime.chain().tick(config, BlurGate.shouldRun(config, client, Runtime.tracker().idle(config)),
                    Runtime.blend());
        }
    }
}
