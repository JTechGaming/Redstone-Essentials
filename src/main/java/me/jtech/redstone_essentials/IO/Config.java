package me.jtech.redstone_essentials.IO;

import eu.midnightdust.lib.config.MidnightConfig;
import me.jtech.redstone_essentials.networking.InfoPackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Entry(category = SETTINGS) public static double ping_range = 40.0;
    @Entry(category = SETTINGS, isColor = true) public static String ping_color = "#ff0000";
    @Entry(category = SETTINGS, isColor = true) public static String multiplayer_ping_color = "#f59e42";
    @Entry(category = SETTINGS) public static List<String> player_colors1 = new ArrayList<>();
    @Entry(category = SETTINGS, isColor = true) public static List<String> player_colors2 = new ArrayList<>();
    @Entry(category = SETTINGS) public static boolean ping_skip_depth_test = false;
    @Entry(category = SETTINGS) public static boolean ping_render_faces = false;
    @Entry(category = SETTINGS) public static boolean send_pings = true;
    @Entry(category = SETTINGS) public static boolean receive_pings = true;
    @Entry(category = SETTINGS) public static boolean announce_clear = true;

    @Comment(category = SETTINGS, centered = true) public static Comment selectionComment;
    @Entry(category = SETTINGS) public static boolean selections_enabled = true;
    @Entry(category = SETTINGS) public static boolean send_selections = true;
    @Entry(category = SETTINGS) public static boolean receive_selections = true;

    @Comment(category = SETTINGS, centered = true) public static Comment rtboComment;
    @Entry(category = SETTINGS) public static boolean rtbo_enabled = true;
    @Entry(category = SETTINGS) public static boolean use_output_labels = true;
    @Entry(category = SETTINGS) public static boolean fill_to_min_size = true;
    @Entry(category = SETTINGS) public static int min_byte_size = 8;
    @Entry(category = SETTINGS) public static BaseMode output_base_select = BaseMode.DEC;
    public enum BaseMode {
        HEX(1), BIN(2), DEC(3), OCT(4);
        private final int value;
        BaseMode(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
    @Entry(category = SETTINGS) public static boolean send_rtbo = true;
    @Entry(category = SETTINGS) public static boolean receive_rtbo = true;

    @Comment(category = SETTINGS, centered = true) public static Comment bitmapComment;
    @Entry(category = SETTINGS) public static boolean bitmap_enabled = true;
    @Entry(category = SETTINGS) public static boolean bitmap_reset_on_finish = true;
    @Entry(category = SETTINGS) public static boolean send_bitmap = true;
    @Entry(category = SETTINGS) public static boolean receive_bitmap = true;


    // Keybindings
    @Comment(category = KEYBINDS, centered = true) public static Comment keybindComment;      // Centered comments are the same as normal ones - just centered!


    public static int getInt(BaseMode enumValue) {
        return enumValue.getValue();
    }

    public static BaseMode getEnum(int value) {
        return BaseMode.values()[value];
    }
}
