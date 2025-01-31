package me.jtech.redstone_essentials.networking.payloads.s2c;

import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.networking.NetworkingPackets;
import me.jtech.redstone_essentials.networking.payloads.c2s.C2SInfoPacket;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

public record S2CInfoPacket(int infoID, String flag1, String flag2, String flag3, List<SelectionData> selections) implements CustomPayload {
    public static final Id<S2CInfoPacket> ID = new Id<>(NetworkingPackets.SERVER_SEND_INFO);
    public static final PacketCodec<RegistryByteBuf, S2CInfoPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, S2CInfoPacket::infoID,
            PacketCodecs.STRING, S2CInfoPacket::flag1,
            PacketCodecs.STRING, S2CInfoPacket::flag2,
            PacketCodecs.STRING, S2CInfoPacket::flag3,
            SelectionData.LIST_PACKET_CODEC, S2CInfoPacket::selections,
            S2CInfoPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
