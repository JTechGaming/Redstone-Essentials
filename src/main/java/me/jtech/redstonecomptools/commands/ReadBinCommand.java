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

public class ReadBinCommand { // TODO comment this
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("rb")
                    .executes(ReadBinCommand::noArgs)
                    .then(CommandManager.argument("invertDirection", BoolArgumentType.bool())
                            .executes(ReadBinCommand::noArgs)
                            .then(CommandManager.argument("offset", IntegerArgumentType.integer())
                                    .executes(ReadBinCommand::noArgs)
                                    .then(CommandManager.argument("c1pos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                            .executes(ReadBinCommand::noArgs)
                                            .then(CommandManager.argument("c1pos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                    .executes(ReadBinCommand::executeCommand))))));
            dispatcher.register(CommandManager.literal("read")
                    .executes(ReadBinCommand::noArgs)
                    .then(CommandManager.argument("invertDirection", BoolArgumentType.bool())
                            .executes(ReadBinCommand::noArgs)
                            .then(CommandManager.argument("offset", IntegerArgumentType.integer())
                                    .executes(ReadBinCommand::noArgs)
                                    .then(CommandManager.argument("c1pos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                            .executes(ReadBinCommand::noArgs)
                                            .then(CommandManager.argument("c1pos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                    .executes(ReadBinCommand::executeCommand))))));
        });
    }

    private static int noArgs(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("You need to provide more arguments!"), false);
        return 1;
    }

    private static int executeCommand(CommandContext<ServerCommandSource> context) {
        BlockPos pos1 = context.getArgument("c1pos1", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos pos2 = context.getArgument("c1pos2", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());

        int offset = IntegerArgumentType.getInteger(context, "offset");

        SelectionHelper selection = new SelectionHelper(pos1, pos2);

        int data = selection.readData(context.getSource().getWorld(), offset);
        context.getSource().sendFeedback(() -> Text.literal(Integer.toBinaryString(data)).withColor(8251903), false);

        return 0;
    }
}
