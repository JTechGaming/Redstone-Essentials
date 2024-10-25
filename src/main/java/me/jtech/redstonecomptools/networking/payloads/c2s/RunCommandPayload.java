package me.jtech.redstonecomptools.networking.payloads.c2s;

import me.jtech.redstonecomptools.networking.NetworkingPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record RunCommandPayload(String command) implements CustomPayload {
    public static final CustomPayload.Id<RunCommandPayload> ID = new CustomPayload.Id<>(NetworkingPackets.RUN_COMMAND_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, RunCommandPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, RunCommandPayload::command, RunCommandPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
