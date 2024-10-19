package me.jtech.redstonecomptools.networking;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;

import java.awt.*;

public record ClientsRenderPingPayload(BlockPos blockPos, Vector3f color, Vector3f size) implements CustomPayload {
    public static final Id<ClientsRenderPingPayload> ID = new Id<>(NetworkingPackets.CLIENTS_RENDER_PING);
    public static final PacketCodec<RegistryByteBuf, ClientsRenderPingPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ClientsRenderPingPayload::blockPos,
            PacketCodecs.VECTOR3F, ClientsRenderPingPayload::color,
            PacketCodecs.VECTOR3F, ClientsRenderPingPayload::size,
            ClientsRenderPingPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
