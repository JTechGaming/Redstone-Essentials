package me.jtech.redstone_essentials.debugger;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DebugManager {
    public static Map<UUID, DebuggerSession> debugSessions = new HashMap<>();
    public static Map<UUID, DebuggerSession> activeDebugSessions = new HashMap<>();

    public static void startDebuggerSession(ServerPlayerEntity player) {
        Path path = Path.of("debugger_session.json");
        debugSessions.put(player.getUuid(), new DebuggerSession(path, player.getServerWorld(), player));
        activeDebugSessions.put(player.getUuid(), debugSessions.get(player.getUuid()));
    }

    public static DebuggerSession getSession(ServerPlayerEntity player) {
        if (debugSessions == null) {
            return null;
        }
        return debugSessions.get(player.getUuid());
    }
}
