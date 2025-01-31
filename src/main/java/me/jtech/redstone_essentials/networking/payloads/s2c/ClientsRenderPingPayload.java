package me.jtech.redstone_essentials.networking.payloads.s2c;

import me.jtech.redstone_essentials.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public record ClientsRenderPingPayload(BlockPos blockPos, Vector3f rgb, Vector3f size, Vector3f bools, String label, String owningPlayer) implements CustomPayload {
    public static final Id<ClientsRenderPingPayload> ID = new Id<>(NetworkingPackets.CLIENTS_RENDER_PING);
    public static final PacketCodec<RegistryByteBuf, ClientsRenderPingPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ClientsRenderPingPayload::blockPos,
            PacketCodecs.VECTOR_3F, ClientsRenderPingPayload::rgb,
            PacketCodecs.VECTOR_3F, ClientsRenderPingPayload::size,
            PacketCodecs.VECTOR_3F, ClientsRenderPingPayload::bools,
            PacketCodecs.STRING, ClientsRenderPingPayload::label,
            PacketCodecs.STRING, ClientsRenderPingPayload::owningPlayer,
            ClientsRenderPingPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
