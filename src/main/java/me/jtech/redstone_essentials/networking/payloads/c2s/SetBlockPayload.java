package me.jtech.redstone_essentials.networking.payloads.c2s;

import me.jtech.redstone_essentials.networking.NetworkingPackets;
import net.minecraft.block.Block;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SetBlockPayload(BlockPos blockPos, String blockName, String supportBlockName) implements CustomPayload {
    public static final Id<SetBlockPayload> ID = new Id<>(NetworkingPackets.SERVER_SET_BLOCK);
    public static final PacketCodec<RegistryByteBuf, SetBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetBlockPayload::blockPos,
            PacketCodecs.STRING, SetBlockPayload::blockName,
            PacketCodecs.STRING, SetBlockPayload::supportBlockName,
            SetBlockPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
