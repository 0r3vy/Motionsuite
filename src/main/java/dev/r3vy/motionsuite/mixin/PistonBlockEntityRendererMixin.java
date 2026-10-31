package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.PistonBlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntityRenderer.class)
public abstract class PistonBlockEntityRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/render/block/entity/state/PistonBlockEntityRenderState;"
                    + "Lnet/minecraft/client/util/math/MatrixStack;"
                    + "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;"
                    + "Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void motionsuite$hidePistons(PistonBlockEntityRenderState state, MatrixStack matrices,
                                         OrderedRenderCommandQueue queue, CameraRenderState camera, CallbackInfo ci) {
        if (Runtime.config().farm().blockRenderCull()) {
            ci.cancel();
        }
    }
}
