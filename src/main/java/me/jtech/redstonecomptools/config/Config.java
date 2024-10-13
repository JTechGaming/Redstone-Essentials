package me.jtech.redstonecomptools.config;

import eu.midnightdust.lib.config.MidnightConfig;

/**
 * <h1>Class for defining values used in the config file using the midnightconfig library</h1>
 * <p>
 * The structure, annotations and variable types to use here
 * are taken straight from the midnightconfig's wiki page at:
 * <a href="https://www.midnightdust.eu/wiki/midnightlib/">...</a>
 * </p>
 */
public class Config extends MidnightConfig {
    public static final String SETTINGS = "settings";
    public static final String KEYBINDS = "keybindings";

    @Comment(category = KEYBINDS, centered = true) public static Comment keybindComment;      // Centered comments are the same as normal ones - just centered!
}
