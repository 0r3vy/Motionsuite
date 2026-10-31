package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.RenderToggle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void motionsuite$noScoreboard(DrawContext context, RenderTickCounter counter, CallbackInfo ci) {
        if (Runtime.config().render().hides(RenderToggle.SCOREBOARD)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void motionsuite$noHotbarText(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Runtime.config().render().hides(RenderToggle.HOTBAR_TEXT)) {
            ci.cancel();
        }
    }
}
