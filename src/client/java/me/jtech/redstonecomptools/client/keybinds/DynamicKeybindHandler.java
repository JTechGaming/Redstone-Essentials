package me.jtech.redstonecomptools.client.keybinds;

import me.jtech.redstonecomptools.client.utility.Pair;
import me.jtech.redstonecomptools.networking.GiveItemPayload;
import me.jtech.redstonecomptools.networking.RunCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.Map;

public class DynamicKeybindHandler {
    public static Map<String, Pair<Integer, DynamicKeybindProperties>> keyBinds = new HashMap<>();

    public static void addKeybind(String name, int key, DynamicKeybindProperties properties) {
        keyBinds.put(name, new Pair<>(key, properties));
    }

    public static void checkKeyPresses() {
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        for (Pair<Integer, DynamicKeybindProperties> pair : keyBinds.values()) {
            if (InputUtil.isKeyPressed(windowHandle, pair.getFirst())) {
                handleKeyPress(pair.getSecond());
            }
        }
    }

    public static void handleKeyPress(DynamicKeybindProperties properties) {
        ClientPlayNetworking.send(new RunCommandPayload(properties.command));
    }
}
