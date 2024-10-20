package me.jtech.redstonecomptools.networking;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public record SetBlockPayload(BlockPos blockPos) implements CustomPayload {
    public static final CustomPayload.Id<SetBlockPayload> ID = new CustomPayload.Id<>(NetworkingPackets.SERVER_SET_BLOCK);
    public static final PacketCodec<RegistryByteBuf, SetBlockPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetBlockPayload::blockPos,
            SetBlockPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
