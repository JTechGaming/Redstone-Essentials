package me.jtech.redstonecomptools.utility;

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
        if (frozen) {
            //Toaster
        } else {

        }
    }
}
