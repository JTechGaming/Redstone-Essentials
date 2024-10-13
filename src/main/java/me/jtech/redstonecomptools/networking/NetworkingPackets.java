package me.jtech.redstonecomptools.networking;

import net.minecraft.util.Identifier;

// Just a helper class to store the packet identifiers
public class NetworkingPackets {
    public static final Identifier GIVE_ITEM_PACKET_ID = Identifier.of("redstonecomptools", "server_give_item");
    public static final Identifier SET_ITEM_PACKET_ID = Identifier.of("redstonecomptools", "server_set_item");
    public static final Identifier RUN_COMMAND_PACKET_ID = Identifier.of("redstonecomptools", "server_run_command");
}