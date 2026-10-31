package dev.r3vy.motionsuite.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.RenderToggle;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureMixin {
    @Shadow
    private boolean dirty;

    @Unique
    private boolean motionsuite$fullbrightWasOn;

    @Inject(method = "update", at = @At("HEAD"))
    private void motionsuite$syncFullbright(float tickProgress, CallbackInfo ci) {
        boolean active = Runtime.config().render().hides(RenderToggle.FULLBRIGHT);
        if (active || this.motionsuite$fullbrightWasOn) {
            this.dirty = true;
        }
        this.motionsuite$fullbrightWasOn = active;
    }

    @ModifyExpressionValue(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z",
                    ordinal = 0),
            require = 1)
    private boolean motionsuite$claimNightVision(boolean original) {
        return Runtime.config().render().claimsNightVision() || original;
    }

    @ModifyExpressionValue(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F"),
            require = 1)
    private static float motionsuite$nightVisionStrength(float original) {
        if (Runtime.config().render().claimsNightVision()) {
            return (float) Runtime.config().render().fullbright();
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Double;floatValue()F",
                    ordinal = 1),
            require = 1)
    private float motionsuite$boostGamma(float original) {
        return Runtime.config().render().gammaBoost(original);
    }
}
