package me.jtech.redstone_essentials.client.rendering.screen.keybinds;

import me.jtech.redstone_essentials.client.keybinds.DynamicKeybindHandler;
import me.jtech.redstone_essentials.client.keybinds.DynamicKeybindProperties;

import java.util.ArrayList;
import java.util.List;

public class KeybindRegistry { //TODO comment this
    private static List<KeybindEntry> keybinds = new ArrayList<>();

    public static void register(KeybindEntry keybind, DynamicKeybindProperties properties) {
        keybinds.add(keybind);
        properties.command = keybind.getCommand();
        DynamicKeybindHandler.addKeybind(keybind.getName(), keybind.getKey(), properties);
    }

    public static void remove(KeybindEntry keybind) {
        keybinds.remove(keybind);
    }

    public static List<KeybindEntry> getKeybinds() {
        return keybinds;
    }
}

