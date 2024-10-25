package me.jtech.redstonecomptools.networking.payloads.s2c;

import me.jtech.redstonecomptools.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record ClientSetBlockPayload(BlockPos blockPos, String blockName) implements CustomPayload {
    public static final Id<ClientSetBlockPayload> ID = new Id<>(NetworkingPackets.CLIENT_SET_BLOCK);
    public static final PacketCodec<RegistryByteBuf, ClientSetBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ClientSetBlockPayload::blockPos,
            PacketCodecs.STRING, ClientSetBlockPayload::blockName,
            ClientSetBlockPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
