package me.jtech.redstonecomptools.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstonecomptools.utility.SelectionHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public class WriteBinCommand {
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("wb")
                    .then(CommandManager.argument("invertDirection", BoolArgumentType.bool())
                            .then(CommandManager.argument("c1pos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                    .then(CommandManager.argument("c1pos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                            .then(CommandManager.argument("value", IntegerArgumentType.integer())
                            .executes(WriteBinCommand::executeCommand))))));
            dispatcher.register(CommandManager.literal("write")
                    .then(CommandManager.argument("invertDirection", BoolArgumentType.bool())
                            .then(CommandManager.argument("c1pos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                    .then(CommandManager.argument("c1pos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                            .then(CommandManager.argument("value", IntegerArgumentType.integer())
                            .executes(WriteBinCommand::executeCommand))))));
        });
    }

    private static int executeCommand(CommandContext<ServerCommandSource> context) {
        BlockPos pos1 = context.getArgument("c1pos1", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos pos2 = context.getArgument("c1pos2", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());

        int data = IntegerArgumentType.getInteger(context, "value");

        SelectionHelper selection = new SelectionHelper(pos1, pos2);

        selection.writeData(context.getSource().getWorld(), data, SelectionHelper.Mode.WRITE);
        return 0;
    }
}
