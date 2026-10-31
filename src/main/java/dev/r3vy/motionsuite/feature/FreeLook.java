package dev.r3vy.motionsuite.feature;

import dev.r3vy.motionsuite.config.CameraConfig;
import dev.r3vy.motionsuite.config.ConfigSnapshot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public final class FreeLook {
    private static final float DEGREES_PER_UNIT = 0.15f;
    private static final float MAX_PITCH = 90.0f;
    private static final float FLIP_THRESHOLD = 90.0f;
    private static final float HALF_TURN = 180.0f;
    private final float[] look = new float[2];

    private final MinecraftClient client;
    private final KeyBinding binding;
    private boolean active;
    private boolean perspectiveForced;
    private float yaw;
    private float pitch;

    public FreeLook(MinecraftClient client, KeyBinding binding) {
        this.client = client;
        this.binding = binding;
    }

    public boolean active() {
        return active;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public void tick(ConfigSnapshot config, FrameBlend blend) {
        boolean toggled = consumePresses();
        if (!usable(config)) {
            if (active) {
                stop(blend);
            }
            return;
        }
        if (config.look().toggleMode()) {
            if (toggled) {
                if (active) {
                    stop(blend);
                } else {
                    start(config, blend);
                }
            }
            return;
        }
        boolean held = binding.isPressed();
        if (held && !active) {
            start(config, blend);
        } else if (!held && active) {
            stop(blend);
        }
    }

    public boolean consume(double cursorDeltaX, double cursorDeltaY) {
        if (!active) {
            return false;
        }
        yaw += (float) cursorDeltaX * DEGREES_PER_UNIT;
        pitch = MathHelper.clamp(pitch + (float) cursorDeltaY * DEGREES_PER_UNIT, -MAX_PITCH, MAX_PITCH);
        return true;
    }

    public float[] cameraLook(float sourceYaw) {
        ClientPlayerEntity player = client.player;
        boolean flipped = player != null
                && Math.abs(MathHelper.wrapDegrees(sourceYaw - player.getYaw())) > FLIP_THRESHOLD;
        look[0] = flipped ? yaw + HALF_TURN : yaw;
        look[1] = flipped ? -pitch : pitch;
        return look;
    }

    private boolean consumePresses() {
        boolean odd = false;
        while (binding.wasPressed()) {
            odd = !odd;
        }
        return odd;
    }

    private boolean usable(ConfigSnapshot config) {
        return config.look().enabled()
                && client.player != null
                && client.world != null
                && client.currentScreen == null
                && client.getCameraEntity() == client.player;
    }

    private void start(ConfigSnapshot config, FrameBlend blend) {
        ClientPlayerEntity player = client.player;
        yaw = player.getYaw();
        pitch = player.getPitch();
        perspectiveForced = false;
        if (config.look().thirdPerson() && client.options.getPerspective().isFirstPerson()) {
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            perspectiveForced = true;
        }
        active = true;
        blend.cut();
    }

    private void stop(FrameBlend blend) {
        active = false;
        if (perspectiveForced && client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            client.options.setPerspective(Perspective.FIRST_PERSON);
        }
        perspectiveForced = false;
        blend.cut();
    }
}
