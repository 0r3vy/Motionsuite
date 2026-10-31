package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.feature.ChurnGuard;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(method = "handleBlockUpdate", at = @At("HEAD"), cancellable = true)
    private void motionsuite$dropChurn(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        if (motionsuite$churn(pos, state)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean motionsuite$churn(BlockPos pos, BlockState state) {
        ChurnGuard guard = Runtime.churn();
        long key = pos.asLong();
        if (!Runtime.config().farm().blockRenderCull()) {
            if (!guard.isEmpty()) {
                guard.thaw(key);
            }
            return false;
        }
        BlockState current = ((ClientWorld) (Object) this).getBlockState(pos);
        if (state.isOf(Blocks.MOVING_PISTON) || state.isOf(Blocks.PISTON_HEAD)) {
            guard.hold(key, state, 0, true);
            return true;
        }
        if (current.isOf(Blocks.MOVING_PISTON) || current.isOf(Blocks.PISTON_HEAD)) {
            guard.thaw(key);
            return false;
        }
        if (guard.frozen(key)) {
            guard.hold(key, state, ChurnGuard.MAX_DEPTH, true);
            return true;
        }
        if (state != current) {
            int depth = guard.nearDepth(key);
            if (depth < ChurnGuard.MAX_DEPTH) {
                guard.hold(key, state, depth + 1, true);
                return true;
            }
            if (state.getBlock() == current.getBlock() && Runtime.config().farm().cullsBlock(state.getBlock())) {
                guard.hold(key, state, ChurnGuard.MAX_DEPTH, false);
                return true;
            }
        }
        guard.thaw(key);
        return false;
    }
}
