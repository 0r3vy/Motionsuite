package dev.r3vy.motionsuite.vk;

import dev.r3vy.motionsuite.Runtime;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.pass.DefaultMainPass;

public final class VkSetup {
    private static boolean hooked;

    private VkSetup() {
    }

    public static void install() {
        if (hooked) {
            return;
        }
        Renderer.getInstance().setMainPass(new BlurPass(DefaultMainPass.create()));
        hooked = true;
    }

    public static void tick() {
        BlurPass.onClientTick(Runtime.config(), Runtime.tracker());
    }

    public static void disconnect() {
        BlurPass.onDisconnect();
    }
}
