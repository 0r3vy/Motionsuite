package dev.r3vy.motionsuite.feature;

import dev.r3vy.motionsuite.config.BlurConfig;
import dev.r3vy.motionsuite.config.ConfigSnapshot;

public final class FrameBlend {
    private static final double MAX_DT = 0.1;
    private static final float QUANTUM = 64.0f;
    private static final float MAX_BLEND = 0.99f;

    private final AdaptiveBlend adaptive = new AdaptiveBlend();

    private boolean pendingSeed = true;
    private long last;

    public void reset() {
        pendingSeed = true;
        last = 0L;
        adaptive.reset();
    }

    public void cut() {
        pendingSeed = true;
    }

    public float next(ConfigSnapshot config, MotionTracker tracker, CameraGlide glide) {
        long now = System.nanoTime();
        if (pendingSeed) {
            pendingSeed = false;
            last = now;
            return 0.0f;
        }
        double dt = last == 0L ? 0.0 : (now - last) / 1.0E9;
        last = now;

        float base = targetBase(config, Math.min(Math.max(dt, 0.0), MAX_DT), tracker.level(dt),
                glide.guardLevel());
        BlurConfig blur = config.blur();
        if (!blur.adaptive() || base <= 0.0f) {
            return quantize(base);
        }
        return quantize(adaptive.adapt(dt, base));
    }

    private static float targetBase(ConfigSnapshot config, double dt, double motionLevel, double guardLevel) {
        BlurConfig blur = config.blur();
        double scale = 1.0 - guardLevel * blur.guardCut() / 100.0;
        if (blur.smart()) {
            double floor = blur.stillBlur() / 100.0;
            scale *= floor + (1.0 - floor) * motionLevel;
        }
        return (float) (blur.blend() * scale);
    }

    private static float quantize(float value) {
        return Math.min(Math.round(value * QUANTUM) / QUANTUM, MAX_BLEND);
    }
}
