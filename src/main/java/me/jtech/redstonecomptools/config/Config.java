package me.jtech.redstonecomptools.config;

import eu.midnightdust.lib.config.MidnightConfig;

/**
 * <h1>Class for defining values used in the config file using the midnightconfig library</h1>
 * <p>
 * The structure, annotations and variable types to use here
 * are taken straight from the midnightconfig wiki page at:
 * <a href="https://www.midnightdust.eu/wiki/midnightlib/">...</a>
 * </p>
 */
public class Config extends MidnightConfig {
    public static final String SETTINGS = "settings";
    public static final String KEYBINDS = "keybindings";

    // Settings
    @Comment(category = SETTINGS, centered = true) public static Comment generalComment;
    @Entry(category = SETTINGS) public static boolean use_toasts = true;

    @Comment(category = SETTINGS, centered = true) public static Comment pingComment;
    @Entry(category = SETTINGS) public static boolean pings_enabled = true;
    @Entry(category = SETTINGS, isColor = true) public static String ping_color = "#ff0000";
    @Entry(category = SETTINGS, isColor = true) public static String multiplayer_ping_color = "#f59e42";
    @Entry(category = SETTINGS) public static boolean send_pings = true;
    @Entry(category = SETTINGS) public static boolean receive_pings = true;

    @Comment(category = SETTINGS, centered = true) public static Comment selectionComment;
    @Entry(category = SETTINGS) public static boolean selections_enabled = true;
    @Entry(category = SETTINGS) public static boolean send_selections = true;
    @Entry(category = SETTINGS) public static boolean receive_selections = true;

    @Comment(category = SETTINGS, centered = true) public static Comment rtboComment;
    @Entry(category = SETTINGS) public static boolean rtbo_enabled = true;
    @Entry(category = SETTINGS) public static boolean use_output_labels = true;
    @Entry(category = SETTINGS) public static boolean fill_to_min_size = true;
    @Entry(category = SETTINGS) public static Integer min_byte_size = 8;
    @Entry(category = SETTINGS) public static BaseMode output_base_select = BaseMode.DEC;
    public enum BaseMode {
        HEX, BIN, DEC, OCT
    }
    @Entry(category = SETTINGS) public static boolean send_rtbo = true;
    @Entry(category = SETTINGS) public static boolean receive_rtbo = true;


    // Keybindings
    @Comment(category = KEYBINDS, centered = true) public static Comment keybindComment;      // Centered comments are the same as normal ones - just centered!
}
