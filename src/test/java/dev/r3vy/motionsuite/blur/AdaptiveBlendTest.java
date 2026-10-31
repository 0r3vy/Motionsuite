package dev.r3vy.motionsuite.blur;

import dev.r3vy.motionsuite.feature.AdaptiveBlend;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdaptiveBlendTest {
    private static final double STEADY = 1.0 / 240.0;
    private static final float BASE = 0.6f;

    @Test
    void returnsBaseWhileLearning() {
        AdaptiveBlend blend = new AdaptiveBlend();
        assertEquals(BASE, blend.adapt(STEADY, BASE));
    }

    @Test
    void staysOnBaseForSteadyFramePacing() {
        AdaptiveBlend blend = new AdaptiveBlend();
        for (int frame = 0; frame < 40; frame++) {
            assertEquals(BASE, blend.adapt(STEADY, BASE),
                    "frame " + frame + " drifted from base");
        }
    }

    @Test
    void shortensTrailOnSlowFrame() {
        AdaptiveBlend blend = new AdaptiveBlend();
        for (int frame = 0; frame < 30; frame++) {
            blend.adapt(STEADY, BASE);
        }
        float result = blend.adapt(0.09, BASE);
        assertTrue(result < BASE, "slow frame must shorten the trail");
        assertTrue(result >= BASE * BASE * 0.99f, "slow frame must not over-darken below ratio clamp");
    }

    @Test
    void relearnsAfterSustainedOffPacing() {
        AdaptiveBlend blend = new AdaptiveBlend();
        for (int frame = 0; frame < 30; frame++) {
            blend.adapt(STEADY, BASE);
        }
        for (int frame = 0; frame < 16; frame++) {
            blend.adapt(0.09, BASE);
        }
        assertEquals(BASE, blend.adapt(0.09, BASE), "sustained off pacing must restart learning");
    }

    @Test
    void ignoresZeroDelta() {
        AdaptiveBlend blend = new AdaptiveBlend();
        for (int frame = 0; frame < 30; frame++) {
            blend.adapt(STEADY, BASE);
        }
        assertEquals(BASE, blend.adapt(0.0, BASE));
    }

    @Test
    void resetClearsLearnedPacing() {
        AdaptiveBlend blend = new AdaptiveBlend();
        for (int frame = 0; frame < 30; frame++) {
            blend.adapt(STEADY, BASE);
        }
        blend.reset();
        assertEquals(BASE, blend.adapt(STEADY, BASE), "reset must return to learning state");
    }
}
