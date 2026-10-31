package dev.r3vy.motionsuite.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.core.FaultGate;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void motionsuite$smoothPosition(Camera camera, double x, double y, double z, Operation<Void> original) {
        double[] smoothed;
        try {
            smoothed = Runtime.glide().apply(Runtime.config(), x, y, z, Runtime.blend());
        } catch (RuntimeException error) {
            Runtime.faults().record(FaultGate.Channel.CAMERA, "camera", error);
            original.call(camera, x, y, z);
            return;
        }
        if (smoothed == null
                || Double.isNaN(smoothed[0]) || Double.isNaN(smoothed[1]) || Double.isNaN(smoothed[2])) {
            original.call(camera, x, y, z);
            return;
        }
        original.call(camera, smoothed[0], smoothed[1], smoothed[2]);
    }

    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void motionsuite$freeLook(Camera camera, float yaw, float pitch, Operation<Void> original) {
        if (!Runtime.look().active()) {
            original.call(camera, yaw, pitch);
            return;
        }
        float[] look = Runtime.look().cameraLook(yaw);
        original.call(camera, look[0], look[1]);
    }
}
