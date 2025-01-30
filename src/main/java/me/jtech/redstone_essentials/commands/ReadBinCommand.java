package me.jtech.redstone_essentials.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstone_essentials.networking.InfoPackets;
import me.jtech.redstone_essentials.networking.NetworkingPackets;
import me.jtech.redstone_essentials.networking.payloads.s2c.S2CInfoPacket;
import me.jtech.redstone_essentials.utility.IClientSelectionContext;
import me.jtech.redstone_essentials.utility.SelectionHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.NetworkUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.ArrayList;

public class ReadBinCommand implements IClientSelectionContext { // TODO comment this
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("rb")
                    .executes(ReadBinCommand::executeCommand));
            dispatcher.register(CommandManager.literal("read")
                    .executes(ReadBinCommand::executeCommand));
        });
    }

    private static int executeCommand(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getPlayer() == null) {
            return 1;
        }

        ServerPlayNetworking.send(context.getSource().getPlayer(), new S2CInfoPacket(InfoPackets.getInt(InfoPackets.S2C.RW_BIN), "read", "", "", new ArrayList<>()));

        return 0;
    }

    @Override
    public void recall(BlockPos blockPos, Color color, Vec3i size, int id, boolean wasModified) {

    }
}
