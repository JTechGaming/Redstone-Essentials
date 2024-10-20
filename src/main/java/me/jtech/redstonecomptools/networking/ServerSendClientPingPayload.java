package me.jtech.redstonecomptools.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public record ServerSendClientPingPayload (BlockPos blockPos, Vector3f rgb, Vector3f size, boolean isSelectionOverlay, boolean isRTBOOverlay, String label) implements CustomPayload {
    public static final CustomPayload.Id<ServerSendClientPingPayload> ID = new CustomPayload.Id<>(NetworkingPackets.SERVER_SEND_PING);
    public static final PacketCodec<RegistryByteBuf, ServerSendClientPingPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ServerSendClientPingPayload::blockPos,
            PacketCodecs.VECTOR3F, ServerSendClientPingPayload::rgb,
            PacketCodecs.VECTOR3F, ServerSendClientPingPayload::size,
            PacketCodecs.BOOL, ServerSendClientPingPayload::isSelectionOverlay,
            PacketCodecs.BOOL, ServerSendClientPingPayload::isRTBOOverlay,
            PacketCodecs.STRING, ServerSendClientPingPayload::label,
            ServerSendClientPingPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
