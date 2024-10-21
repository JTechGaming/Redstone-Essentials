package me.jtech.redstonecomptools.networking;

import net.minecraft.util.Identifier;

// Just a helper class to store the packet identifiers
public class NetworkingPackets {
    public static final Identifier GIVE_ITEM_PACKET_ID = Identifier.of("redstonecomptools", "server_give_item");
    public static final Identifier SET_ITEM_PACKET_ID = Identifier.of("redstonecomptools", "server_set_item");
    public static final Identifier RUN_COMMAND_PACKET_ID = Identifier.of("redstonecomptools", "server_run_command");
    public static final Identifier CLIENTS_RENDER_PING = Identifier.of("redstonecomptools", "clients_render_ping");
    public static final Identifier SERVER_SEND_PING = Identifier.of("redstonecomptools", "server_send_ping");
    public static final Identifier SERVER_SET_BLOCK = Identifier.of("redstonecomptools", "server_set_block");
    public static final Identifier CLIENT_OPEN_SCREEN = Identifier.of("redstonecomptools", "client_open_screen");
    public static final Identifier CLIENT_FINISH_BITMAP_PRINT = Identifier.of("redstonecomptools", "client_finish_bitmap_print");
}