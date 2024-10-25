package me.jtech.redstonecomptools.networking.payloads.s2c;

import me.jtech.redstonecomptools.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record OpenScreenPayload(int id) implements CustomPayload {
    public static final Id<OpenScreenPayload> ID = new Id<>(NetworkingPackets.CLIENT_OPEN_SCREEN);
    public static final PacketCodec<RegistryByteBuf, OpenScreenPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, OpenScreenPayload::id, OpenScreenPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
