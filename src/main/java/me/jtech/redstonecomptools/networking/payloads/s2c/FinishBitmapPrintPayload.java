package me.jtech.redstonecomptools.networking.payloads.s2c;

import me.jtech.redstonecomptools.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record FinishBitmapPrintPayload(boolean successful) implements CustomPayload {
    public static final CustomPayload.Id<FinishBitmapPrintPayload> ID = new CustomPayload.Id<>(NetworkingPackets.CLIENT_FINISH_BITMAP_PRINT);
    public static final PacketCodec<RegistryByteBuf, FinishBitmapPrintPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, FinishBitmapPrintPayload::successful,
            FinishBitmapPrintPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
