package dev.r3vy.motionsuite.mixin;

import dev.r3vy.motionsuite.Runtime;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onLightUpdate", at = @At("HEAD"), cancellable = true, require = 0)
    private void motionsuite$freezeLight(LightUpdateS2CPacket packet, CallbackInfo ci) {
        if (Runtime.config().farm().blockRenderCull() && !Runtime.churn().isEmpty()) {
            ci.cancel();
        }
    }

    @Inject(method = "onBlockEvent", at = @At("HEAD"), cancellable = true, require = 0)
    private void motionsuite$freezeAction(BlockEventS2CPacket packet, CallbackInfo ci) {
        if (!Runtime.config().farm().blockRenderCull()) {
            return;
        }
        Block block = packet.getBlock();
        if (Runtime.config().farm().cullsBlock(block)
                && (block instanceof PistonBlock || block instanceof DoorBlock || block instanceof TrapdoorBlock)) {
            ci.cancel();
        }
    }
}
