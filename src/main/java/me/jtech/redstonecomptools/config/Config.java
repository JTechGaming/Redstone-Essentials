package me.jtech.redstonecomptools.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class Config extends MidnightConfig {
    public static final String SETTINGS = "settings";
    public static final String KEYBINDS = "keybindings";

    @Comment(category = KEYBINDS, centered = true) public static Comment keybindComment;      // Centered comments are the same as normal ones - just centered!
}
