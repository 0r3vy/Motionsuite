package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.PostEffectControl;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements PostEffectControl {
    @Shadow
    private Identifier postProcessorId;

    @Shadow
    private boolean postProcessorEnabled;

    @Override
    public Identifier motionsuite$getEffectId() {
        return postProcessorId;
    }

    @Override
    public void motionsuite$setEffectId(Identifier id) {
        postProcessorId = id;
    }

    @Override
    public void motionsuite$setEffectActive(boolean active) {
        postProcessorEnabled = active;
    }
}
