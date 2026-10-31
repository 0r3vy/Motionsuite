package dev.r3vy.motionsuite.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.core.FaultGate;
import dev.r3vy.motionsuite.feature.BlendUniform;
import net.minecraft.client.gl.PostEffectPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(PostEffectPass.class)
public abstract class PostPassMixin {
    @Shadow
    @Final
    private Map<String, GpuBuffer> uniformBuffers;

    @Unique
    private GpuBuffer motionsuite$owned;

    @Unique
    private float motionsuite$ownedBlend = -1.0f;

    @Inject(method = "render", at = @At("HEAD"))
    private void motionsuite$refreshBlend(CallbackInfo ci) {
        if (!Runtime.blurWanted() || Runtime.faults().isBroken(FaultGate.Channel.BLUR)) {
            return;
        }
        GpuBuffer current = uniformBuffers.get(BlendUniform.NAME);
        if (current == null) {
            return;
        }
        float blend = Runtime.blend().next(Runtime.config(), Runtime.tracker(), Runtime.glide());
        if (current == motionsuite$owned) {
            if (blend == motionsuite$ownedBlend) {
                return;
            }
            if (BlendUniform.update(current, blend, Runtime.faults())) {
                motionsuite$ownedBlend = blend;
            }
            return;
        }
        GpuBuffer replacement = BlendUniform.create(blend, Runtime.faults());
        if (replacement == null) {
            return;
        }
        uniformBuffers.put(BlendUniform.NAME, replacement);
        motionsuite$owned = replacement;
        motionsuite$ownedBlend = blend;
        current.close();
    }
}
