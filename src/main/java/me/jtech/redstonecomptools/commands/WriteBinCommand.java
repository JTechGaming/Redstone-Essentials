package me.jtech.redstonecomptools.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class WriteBinCommand {
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("wb")
                    .then(CommandManager.argument("offset", IntegerArgumentType.integer())
                            .then(CommandManager.argument("isVertical", BoolArgumentType.bool()))
                            .then(CommandManager.argument("invertDirection", BoolArgumentType.bool()))
                            .executes(WriteBinCommand::executeCommand)));
            dispatcher.register(CommandManager.literal("write")
                    .then(CommandManager.argument("offset", IntegerArgumentType.integer())
                            .then(CommandManager.argument("isVertical", BoolArgumentType.bool()))
                            .then(CommandManager.argument("invertDirection", BoolArgumentType.bool()))
                            .executes(WriteBinCommand::executeCommand)));
        });
    }

    private static int executeCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        return 0;
    }
}
