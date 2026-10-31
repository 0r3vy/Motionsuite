package dev.r3vy.motionsuite.feature;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.r3vy.motionsuite.core.FaultGate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class BlendUniform {
    public static final String NAME = "MotionCfg";

    private static final Logger LOGGER = LoggerFactory.getLogger("motionsuite");
    private static final int BLOCK_SIZE = 16;
    private static final int USAGE_MAP_WRITE = 0x02;
    private static final int USAGE_UNIFORM = 0x80;
    private static final int UNIFORM_MAP_WRITE_FLAGS = USAGE_MAP_WRITE | USAGE_UNIFORM;
    private static boolean failureReported;

    private BlendUniform() {
    }

    public static GpuBuffer create(float blend, FaultGate faults) {
        try {
            ByteBuffer data = ByteBuffer.allocateDirect(BLOCK_SIZE).order(ByteOrder.nativeOrder());
            data.putFloat(0, blend);
            return RenderSystem.getDevice().createBuffer(() -> "motionsuite blend",
                    UNIFORM_MAP_WRITE_FLAGS, data);
        } catch (RuntimeException e) {
            report(e, faults);
            return null;
        }
    }

    public static boolean update(GpuBuffer buffer, float blend, FaultGate faults) {
        try {
            var view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(buffer, false, true);
            try {
                view.data().putFloat(0, blend);
            } finally {
                view.close();
            }
            return true;
        } catch (RuntimeException e) {
            report(e, faults);
            return false;
        }
    }

    private static void report(RuntimeException e, FaultGate faults) {
        if (!failureReported) {
            failureReported = true;
            LOGGER.error("Could not update the blend uniform buffer", e);
        }
        faults.recordOnce(FaultGate.Channel.BLUR, "uniform buffer", e);
    }
}
