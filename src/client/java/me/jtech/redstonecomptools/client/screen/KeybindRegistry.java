package me.jtech.redstonecomptools.client.screen;

import me.jtech.redstonecomptools.client.keybinds.DynamicKeybindHandler;
import me.jtech.redstonecomptools.client.keybinds.DynamicKeybindProperties;

import java.util.ArrayList;
import java.util.List;

public class KeybindRegistry {
    private static List<KeybindEntry> keybinds = new ArrayList<>();

    public static void register(KeybindEntry keybind) {
        keybinds.add(keybind);
        DynamicKeybindProperties properties = new DynamicKeybindProperties();
        properties.command = keybind.getCommand();
        //TODO make this work with multi-keybinds like ctrl + i for example
        //DynamicKeybindHandler.addKeybind(keybind.getName(), keybind, properties);
    }

    public static List<KeybindEntry> getKeybinds() {
        return keybinds;
    }
}

