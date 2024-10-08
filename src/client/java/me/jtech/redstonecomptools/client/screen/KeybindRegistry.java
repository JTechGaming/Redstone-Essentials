package me.jtech.redstonecomptools.client.screen;

import java.util.ArrayList;
import java.util.List;

public class KeybindRegistry {
    private static List<KeybindEntry> keybinds = new ArrayList<>();

    public static void register(KeybindEntry keybind) {
        keybinds.add(keybind);
    }

    public static List<KeybindEntry> getKeybinds() {
        return keybinds;
    }
}

