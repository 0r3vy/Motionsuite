package dev.r3vy.motionsuite.feature;

import dev.r3vy.motionsuite.config.BlurConfig;
import dev.r3vy.motionsuite.config.ConfigSnapshot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public final class MotionTracker {
    private static final double ROTATION_FULL_DEGREES = 12.0;
    private static final double MOVEMENT_FULL_BLOCKS = 0.8;
    private static final double TELEPORT_DIST_SQ = 64.0;
    private static final double TICK_SMOOTHING = 0.35;
    private static final double FRAME_RESPONSE_SECONDS = 0.05;
    private static final double IDLE_LEVEL = 0.02;
    private static final int IDLE_TICKS = 10;

    private float lastYaw;
    private float lastPitch;
    private double lastX;
    private double lastY;
    private double lastZ;
    private boolean tracking;
    private boolean lookWasFree;
    private double tickLevel;
    private double frameLevel;
    private int calmTicks;

    public void observe(MinecraftClient client, FreeLook freeLook, FrameBlend blend, boolean cameraSmoothing) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            tracking = false;
            tickLevel = 0.0;
            frameLevel = 0.0;
            calmTicks = 0;
            return;
        }
        boolean free = freeLook.active();
        float yaw = free ? freeLook.yaw() : player.getYaw();
        float pitch = free ? freeLook.pitch() : player.getPitch();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        if (tracking) {
            double turn = free == lookWasFree
                    ? Math.hypot(MathHelper.wrapDegrees(yaw - lastYaw), pitch - lastPitch)
                    : 0.0;
            double dx = x - lastX;
            double dy = y - lastY;
            double dz = z - lastZ;
            double moveSq = dx * dx + dy * dy + dz * dz;
            double move = Math.sqrt(moveSq);
            if (moveSq > TELEPORT_DIST_SQ && !cameraSmoothing) {
                blend.cut();
            }
            double target = Math.min(1.0, turn / ROTATION_FULL_DEGREES + move / MOVEMENT_FULL_BLOCKS);
            tickLevel += (target - tickLevel) * TICK_SMOOTHING;
        }
        calmTicks = tickLevel < IDLE_LEVEL ? calmTicks + 1 : 0;

        lookWasFree = free;
        lastYaw = yaw;
        lastPitch = pitch;
        lastX = x;
        lastY = y;
        lastZ = z;
        tracking = true;
    }

    public double level(double dt) {
        double response = 1.0 - Math.exp(-Math.max(dt, 0.0) / FRAME_RESPONSE_SECONDS);
        frameLevel += (tickLevel - frameLevel) * response;
        return frameLevel;
    }

    public boolean idle(ConfigSnapshot config) {
        BlurConfig blur = config.blur();
        return blur.smart() && blur.stillBlur() == 0 && calmTicks >= IDLE_TICKS;
    }
}
