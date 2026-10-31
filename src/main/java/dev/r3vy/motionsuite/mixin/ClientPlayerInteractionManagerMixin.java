package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackBlock", at = @At("HEAD"))
    private void motionsuite$thawAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Runtime.churn().thaw(pos.asLong());
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void motionsuite$thawBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Runtime.churn().thaw(pos.asLong());
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), require = 0)
    private void motionsuite$thawUse(ClientPlayerEntity player, Hand hand, BlockHitResult hit,
                                     CallbackInfoReturnable<ActionResult> cir) {
        BlockPos pos = hit.getBlockPos();
        Runtime.churn().thaw(pos.asLong());
        Runtime.churn().thaw(pos.offset(hit.getSide()).asLong());
    }
}
