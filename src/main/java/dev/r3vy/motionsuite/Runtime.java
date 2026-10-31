package dev.r3vy.motionsuite;

import dev.r3vy.motionsuite.config.ConfigManager;
import dev.r3vy.motionsuite.config.ConfigSnapshot;
import dev.r3vy.motionsuite.core.FaultGate;
import dev.r3vy.motionsuite.feature.BlurGate;
import dev.r3vy.motionsuite.feature.CameraGlide;
import dev.r3vy.motionsuite.feature.ChurnGuard;
import dev.r3vy.motionsuite.feature.EffectFilter;
import dev.r3vy.motionsuite.feature.FrameBlend;
import dev.r3vy.motionsuite.feature.FreeLook;
import dev.r3vy.motionsuite.feature.HudCleaner;
import dev.r3vy.motionsuite.feature.MotionTracker;
import dev.r3vy.motionsuite.feature.PostChainController;
import dev.r3vy.motionsuite.feature.RenderBudget;
import net.minecraft.client.MinecraftClient;

public final class Runtime {
    private static final ConfigManager CONFIG = new ConfigManager();
    private static final FaultGate FAULTS = new FaultGate();
    private static final EffectFilter FILTER = new EffectFilter();
    private static final ChurnGuard CHURN = new ChurnGuard();
    private static final FrameBlend BLEND = new FrameBlend();
    private static final MotionTracker TRACKER = new MotionTracker();
    private static final RenderBudget BUDGET = new RenderBudget();
    private static final HudCleaner HUD = new HudCleaner();

    private static CameraGlide glide;
    private static FreeLook look;
    private static PostChainController chain;
    private static VulkanBridge vulkan;

    private Runtime() {
    }

    static void init(MinecraftClient client, FreeLook freeLook, VulkanBridge bridge) {
        Runtime.look = freeLook;
        Runtime.glide = new CameraGlide(client, FAULTS);
        Runtime.chain = new PostChainController(client);
        Runtime.vulkan = bridge;
    }

    public static ConfigSnapshot config() {
        return CONFIG.snapshot();
    }

    public static long epoch() {
        return CONFIG.epoch();
    }

    public static ConfigManager configs() {
        return CONFIG;
    }

    public static FaultGate faults() {
        return FAULTS;
    }

    public static EffectFilter filter() {
        return FILTER;
    }

    public static ChurnGuard churn() {
        return CHURN;
    }

    public static FrameBlend blend() {
        return BLEND;
    }

    public static MotionTracker tracker() {
        return TRACKER;
    }

    public static RenderBudget budget() {
        return BUDGET;
    }

    public static HudCleaner hud() {
        return HUD;
    }

    public static CameraGlide glide() {
        return glide;
    }

    public static FreeLook look() {
        return look;
    }

    public static PostChainController chain() {
        return chain;
    }

    public static VulkanBridge vulkan() {
        return vulkan;
    }

    public static boolean blurWanted() {
        return BlurGate.shouldRun(config(), MinecraftClient.getInstance(), TRACKER.idle(config()));
    }
}
