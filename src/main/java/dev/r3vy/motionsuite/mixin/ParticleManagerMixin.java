package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.RenderToggle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Inject(
            method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true)
    private void motionsuite$limitParticles(ParticleEffect parameters, double x, double y, double z,
                                            double velocityX, double velocityY, double velocityZ,
                                            CallbackInfoReturnable<Particle> cir) {
        if (Runtime.config().render().hides(RenderToggle.PARTICLES)
                || !Runtime.filter().allowParticle(parameters.getType(), Runtime.config())) {
            cir.setReturnValue(null);
        }
    }
}
