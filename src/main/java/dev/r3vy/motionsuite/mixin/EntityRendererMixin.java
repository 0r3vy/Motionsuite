package dev.r3vy.motionsuite.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.r3vy.motionsuite.Runtime;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @WrapMethod(method = "shouldRender")
    private boolean motionsuite$budgetEntities(Entity entity, Frustum frustum, double x, double y, double z,
                                               Operation<Boolean> original) {
        return original.call(entity, frustum, x, y, z)
                && Runtime.budget().allow(entity, Runtime.config(), x, y, z);
    }
}
