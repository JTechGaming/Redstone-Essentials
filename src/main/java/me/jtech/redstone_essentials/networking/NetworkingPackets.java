package me.jtech.redstone_essentials.networking;

import net.minecraft.util.Identifier;

// Just a helper class to store the packet identifiers
public class NetworkingPackets {
    public static final Identifier GIVE_ITEM_PACKET_ID = Identifier.of("redstone_essentials", "server_give_item");
    public static final Identifier SET_ITEM_PACKET_ID = Identifier.of("redstone_essentials", "server_set_item");
    public static final Identifier CLIENTS_RENDER_PING = Identifier.of("redstone_essentials", "clients_render_ping");
    public static final Identifier SERVER_SEND_PING = Identifier.of("redstone_essentials", "server_send_ping");
    public static final Identifier SERVER_SET_BLOCK = Identifier.of("redstone_essentials", "server_set_block");
    public static final Identifier CLIENT_SET_BLOCK = Identifier.of("redstone_essentials", "client_set_block");
    public static final Identifier CLIENT_OPEN_SCREEN = Identifier.of("redstone_essentials", "client_open_screen");
    public static final Identifier CLIENT_FINISH_BITMAP_PRINT = Identifier.of("redstone_essentials", "client_finish_bitmap_print");
    public static final Identifier CLIENT_GET_SERVER_BITMAPS = Identifier.of("redstone_essentials", "client_get_server_bitmaps");
    public static final Identifier SERVER_SEND_BITMAPS = Identifier.of("redstone_essentials", "server_send_bitmaps");
    public static final Identifier SERVER_SEND_INFO = Identifier.of("redstone_essentials", "server_info");
    public static final Identifier CLIENT_SEND_INFO = Identifier.of("redstone_essentials", "client_info");
}