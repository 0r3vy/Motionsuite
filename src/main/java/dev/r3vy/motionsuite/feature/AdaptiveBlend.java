package dev.r3vy.motionsuite.feature;

public final class AdaptiveBlend {
    private static final int WINDOW = 24;
    private static final double STABLE_SPREAD = 2.2;
    private static final double SLOW_RATE = 0.02;
    private static final double SLOW_FLOOR = 0.8;
    private static final double SLOW_CEILING = 1.25;
    private static final double MAX_DT = 0.1;
    private static final double MIN_RATIO = 0.5;
    private static final double MAX_RATIO = 2.0;
    private static final double OFF_LOW = 0.7;
    private static final double OFF_HIGH = 1.45;
    private static final int OFF_FRAMES = 16;
    private static final double RATIO_SMOOTHING = 0.5;
    private static final double RELEARNING = -1.0;
    private static final float DEADBAND = 0.01f;

    private final double[] window = new double[WINDOW];
    private final double[] sorted = new double[WINDOW];
    private double slow;
    private double smoothedRatio = 1.0;
    private int count;
    private int head;
    private int off;

    public float adapt(double dt, float base) {
        if (slow == 0.0) {
            learn(dt);
            return base;
        }
        double ratio = measureRatio(dt);
        if (ratio == RELEARNING) {
            return base;
        }
        smoothedRatio += (ratio - smoothedRatio) * RATIO_SMOOTHING;
        double exponent = Math.max(MIN_RATIO, Math.min(MAX_RATIO, smoothedRatio));
        float scaled = (float) Math.pow(base, exponent);
        return Math.abs(scaled - base) < DEADBAND ? base : scaled;
    }

    public void reset() {
        slow = 0.0;
        smoothedRatio = 1.0;
        count = 0;
        head = 0;
        off = 0;
    }

    private double measureRatio(double dt) {
        if (dt >= MAX_DT) {
            return MAX_RATIO;
        }
        if (dt <= 0.0) {
            return 1.0;
        }
        double ratio = dt / slow;
        if (ratio < OFF_LOW || ratio > OFF_HIGH) {
            off++;
            if (off >= OFF_FRAMES) {
                reset();
                return RELEARNING;
            }
        } else {
            off = 0;
        }
        double target = Math.max(SLOW_FLOOR * slow, Math.min(SLOW_CEILING * slow, dt));
        slow += (target - slow) * SLOW_RATE;
        return ratio;
    }

    private void learn(double dt) {
        if (dt <= 0.0 || dt >= MAX_DT) {
            count = 0;
            head = 0;
            return;
        }
        window[head] = dt;
        head = (head + 1) % WINDOW;
        if (count < WINDOW) {
            count++;
        }
        rebuildSorted();
        if (count == WINDOW && sorted[WINDOW - 1] <= STABLE_SPREAD * sorted[0]) {
            int middle = WINDOW / 2;
            slow = (sorted[middle - 1] + sorted[middle]) / 2.0;
            off = 0;
        }
    }

    private void rebuildSorted() {
        int filled = Math.min(count, WINDOW);
        System.arraycopy(window, 0, sorted, 0, filled);
        for (int i = 1; i < filled; i++) {
            double value = sorted[i];
            int at = i - 1;
            while (at >= 0 && sorted[at] > value) {
                sorted[at + 1] = sorted[at];
                at--;
            }
            sorted[at + 1] = value;
        }
    }
}
