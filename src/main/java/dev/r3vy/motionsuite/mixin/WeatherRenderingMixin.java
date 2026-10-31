package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.RenderToggle;
import net.minecraft.client.render.WeatherRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherRendering.class)
public abstract class WeatherRenderingMixin {
    @Inject(method = "buildPrecipitationPieces", at = @At("HEAD"), cancellable = true, require = 0)
    private void motionsuite$noRainGeometry(CallbackInfo ci) {
        if (Runtime.config().render().hides(RenderToggle.WEATHER)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPrecipitation", at = @At("HEAD"), cancellable = true, require = 0)
    private void motionsuite$noRainDraw(CallbackInfo ci) {
        if (Runtime.config().render().hides(RenderToggle.WEATHER)) {
            ci.cancel();
        }
    }

    @Inject(method = "addParticlesAndSound", at = @At("HEAD"), cancellable = true, require = 0)
    private void motionsuite$noRainSplashes(CallbackInfo ci) {
        if (Runtime.config().render().hides(RenderToggle.WEATHER)) {
            ci.cancel();
        }
    }
}
