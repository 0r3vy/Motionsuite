package dev.r3vy.motionsuite.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.r3vy.motionsuite.Lang;
import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.input.KeyBindings;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public final class Commands {
    private static final String ROOT = "motionsuite";

    private Commands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(root()));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> root() {
        return ClientCommandManager.literal(ROOT)
                .executes(context -> openHub())
                .then(ClientCommandManager.literal("hub")
                        .executes(context -> openHub()))
                .then(ClientCommandManager.literal("reload")
                        .executes(context -> reload(context.getSource())));
    }

    private static int openHub() {
        dev.r3vy.motionsuite.ui.HubScreen.open();
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(FabricClientCommandSource source) {
        Runtime.configs().reload();
        KeyBindings.applyStored(Runtime.config().keys());
        Runtime.blend().reset();
        source.sendFeedback(Text.literal(Lang.t("command.reloaded")));
        return Command.SINGLE_SUCCESS;
    }
}
