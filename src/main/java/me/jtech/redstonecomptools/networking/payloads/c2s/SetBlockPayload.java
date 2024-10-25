package me.jtech.redstonecomptools.networking.payloads.c2s;

import me.jtech.redstonecomptools.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SetBlockPayload(BlockPos blockPos, String blockName) implements CustomPayload {
    public static final CustomPayload.Id<SetBlockPayload> ID = new CustomPayload.Id<>(NetworkingPackets.SERVER_SET_BLOCK);
    public static final PacketCodec<RegistryByteBuf, SetBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetBlockPayload::blockPos,
            PacketCodecs.STRING, SetBlockPayload::blockName,
            SetBlockPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
