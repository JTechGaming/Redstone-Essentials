package me.jtech.redstone_essentials.networking.payloads.c2s;

import me.jtech.redstone_essentials.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ClientGetServerBitmapsPayload(String specificBitmap) implements CustomPayload {
    public static final Id<ClientGetServerBitmapsPayload> ID = new Id<>(NetworkingPackets.CLIENT_GET_SERVER_BITMAPS);
    public static final PacketCodec<RegistryByteBuf, ClientGetServerBitmapsPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, ClientGetServerBitmapsPayload::specificBitmap, ClientGetServerBitmapsPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
