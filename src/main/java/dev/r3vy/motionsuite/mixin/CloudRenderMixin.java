package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.RenderToggle;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.CloudRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public abstract class CloudRenderMixin {
    @Inject(
            method = "renderClouds(ILnet/minecraft/client/option/CloudRenderMode;FLnet/minecraft/util/math/Vec3d;JF)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private void motionsuite$noClouds(int centerX, CloudRenderMode mode, float tickProgress,
                                      Vec3d cameraPos, long seed, float cloudHeight, CallbackInfo ci) {
        if (Runtime.config().render().hides(RenderToggle.CLOUDS)) {
            ci.cancel();
        }
    }
}
