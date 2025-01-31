package me.jtech.redstone_essentials.networking.payloads.c2s;

import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

public record C2SInfoPacket(int infoID, String flag1, String flag2, String flag3, List<SelectionData> selections) implements CustomPayload {
    public static final Id<C2SInfoPacket> ID = new Id<>(NetworkingPackets.CLIENT_SEND_INFO);
    public static final PacketCodec<RegistryByteBuf, C2SInfoPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, C2SInfoPacket::infoID,
            PacketCodecs.STRING, C2SInfoPacket::flag1,
            PacketCodecs.STRING, C2SInfoPacket::flag2,
            PacketCodecs.STRING, C2SInfoPacket::flag3,
            SelectionData.LIST_PACKET_CODEC, C2SInfoPacket::selections,
            C2SInfoPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
