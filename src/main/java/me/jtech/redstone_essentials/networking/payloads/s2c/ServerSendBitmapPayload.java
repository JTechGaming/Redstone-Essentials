package me.jtech.redstone_essentials.networking.payloads.s2c;

import me.jtech.redstone_essentials.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ServerSendBitmapPayload(String bitmap, boolean finalBitmap) implements CustomPayload {
    public static final Id<ServerSendBitmapPayload> ID = new Id<>(NetworkingPackets.SERVER_SEND_BITMAPS);
    public static final PacketCodec<RegistryByteBuf, ServerSendBitmapPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ServerSendBitmapPayload::bitmap,
            PacketCodecs.BOOLEAN, ServerSendBitmapPayload::finalBitmap,
            ServerSendBitmapPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
