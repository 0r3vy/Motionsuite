package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.sound.TickableSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {
    @Inject(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void motionsuite$limitSound(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (!Runtime.filter().allowSoundCached(sound.getId())) {
            cir.setReturnValue(SoundSystem.PlayResult.NOT_STARTED);
        }
    }

    @Inject(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void motionsuite$limitDelayedSound(SoundInstance sound, int delay, CallbackInfo ci) {
        if (!Runtime.filter().allowSoundCached(sound.getId())) {
            ci.cancel();
        }
    }

    @Inject(
            method = "playNextTick(Lnet/minecraft/client/sound/TickableSoundInstance;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void motionsuite$limitNextTickSound(TickableSoundInstance sound, CallbackInfo ci) {
        if (!Runtime.filter().allowSoundCached(sound.getId())) {
            ci.cancel();
        }
    }
}
