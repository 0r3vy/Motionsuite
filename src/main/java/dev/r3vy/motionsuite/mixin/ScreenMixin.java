package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.RenderToggle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void motionsuite$noTint(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (Runtime.config().render().hides(RenderToggle.SCREEN_TINT)
                && MinecraftClient.getInstance().world != null) {
            ci.cancel();
        }
    }
}
