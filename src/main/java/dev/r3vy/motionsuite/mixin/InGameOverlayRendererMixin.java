package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.RenderToggle;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void motionsuite$noFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                           Sprite sprite, CallbackInfo ci) {
        if (Runtime.config().render().hides(RenderToggle.FIRE)) {
            ci.cancel();
        }
    }
}
