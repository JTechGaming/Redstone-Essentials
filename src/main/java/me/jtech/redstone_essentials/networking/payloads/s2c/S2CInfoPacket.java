package me.jtech.redstone_essentials.networking.payloads.s2c;

import me.jtech.redstone_essentials.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record S2CInfoPacket(int infoID, String flags) implements CustomPayload {
    public static final CustomPayload.Id<S2CInfoPacket> ID = new CustomPayload.Id<>(NetworkingPackets.SERVER_SEND_INFO);
    public static final PacketCodec<RegistryByteBuf, S2CInfoPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, S2CInfoPacket::infoID,
            PacketCodecs.STRING, S2CInfoPacket::flags,
            S2CInfoPacket::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
