package me.jtech.redstone_essentials.utility;

import net.minecraft.server.ServerTickManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DebuggingHelper {
    private static void executeFreeze(ServerPlayerEntity player, boolean frozen) {
        ServerTickManager serverTickManager = player.getServer().getTickManager();
        if (frozen) {
            if (serverTickManager.isSprinting()) {
                serverTickManager.stopSprinting();
            }

            if (serverTickManager.isStepping()) {
                serverTickManager.stopStepping();
            }
        }

        serverTickManager.setFrozen(frozen);
    }

    private static void executeStep(ServerPlayerEntity player, int steps) {
        ServerTickManager serverTickManager = player.getServer().getTickManager();
        serverTickManager.step(steps);
    }

    private static void executeStopStep(ServerPlayerEntity player) {
        ServerTickManager serverTickManager = player.getServer().getTickManager();
        serverTickManager.stopStepping();
    }

    private static void executeRate(ServerPlayerEntity player, float rate) {
        ServerTickManager serverTickManager = player.getServer().getTickManager();
        serverTickManager.setTickRate(rate);
        String string = String.format("%.1f", rate);
        //return (int)rate;
    }
}
