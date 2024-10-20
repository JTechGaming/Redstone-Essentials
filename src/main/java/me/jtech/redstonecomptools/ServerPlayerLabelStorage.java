package me.jtech.redstonecomptools;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerPlayerLabelStorage {
    public static Map<ServerPlayerEntity, List<RealtimeByteOutput>> playerLabelList = new HashMap<>();

    public static void addPlayerRTBO(ServerPlayerEntity player, RealtimeByteOutput realtimeByteOutput) {
        if (!playerLabelList.containsKey(player)) {
            List<RealtimeByteOutput> list = new ArrayList<>();
            list.add(realtimeByteOutput);
            playerLabelList.put(player, list);
            return;
        }
        playerLabelList.get(player).add(realtimeByteOutput);
    }
}
