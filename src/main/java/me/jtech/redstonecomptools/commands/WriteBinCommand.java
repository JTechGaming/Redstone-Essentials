package me.jtech.redstonecomptools.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstonecomptools.utility.SelectionHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class WriteBinCommand { // TODO comment this
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("wb")
                    .executes(WriteBinCommand::noArgs)
                    .then(CommandManager.argument("invertDirection", BoolArgumentType.bool())
                            .executes(WriteBinCommand::noArgs)
                            .then(CommandManager.argument("c1pos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                    .executes(WriteBinCommand::noArgs)
                                    .then(CommandManager.argument("c1pos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                            .executes(WriteBinCommand::noArgs)
                                            .then(CommandManager.argument("offset", IntegerArgumentType.integer())
                                                    .executes(WriteBinCommand::noArgs)
                                                    .then(CommandManager.argument("value", IntegerArgumentType.integer())
                                                            .executes(WriteBinCommand::executeCommand)))))));
            dispatcher.register(CommandManager.literal("write")
                    .executes(WriteBinCommand::noArgs)
                    .then(CommandManager.argument("invertDirection", BoolArgumentType.bool())
                            .executes(WriteBinCommand::noArgs)
                            .then(CommandManager.argument("c1pos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                    .executes(WriteBinCommand::noArgs)
                                    .then(CommandManager.argument("c1pos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                            .then(CommandManager.argument("offset", IntegerArgumentType.integer())
                                                    .executes(WriteBinCommand::noArgs)
                                                    .then(CommandManager.argument("value", IntegerArgumentType.integer())
                                                            .executes(WriteBinCommand::executeCommand)))))));
        });
    }

    private static int noArgs(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("You need to provide more arguments!"), false);
        return 1;
    }

    private static int executeCommand(CommandContext<ServerCommandSource> context) {
        BlockPos pos1 = context.getArgument("c1pos1", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos pos2 = context.getArgument("c1pos2", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        boolean invertDirection = BoolArgumentType.getBool(context, "invertDirection");

        int offset = IntegerArgumentType.getInteger(context, "offset");
        int data = IntegerArgumentType.getInteger(context, "value");

        SelectionHelper selection = new SelectionHelper(pos1, pos2, invertDirection);

        selection.writeData(context.getSource().getWorld(), data, offset, SelectionHelper.Mode.WRITE, context.getSource().getPlayer());
        return 0;
    }
}
