package me.jtech.redstonecomptools.networking;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.w3c.dom.Text;

public record RunCommandPayload(String command) implements CustomPayload {
    public static final CustomPayload.Id<RunCommandPayload> ID = new CustomPayload.Id<>(NetworkingPackets.RUN_COMMAND_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, RunCommandPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, RunCommandPayload::command, RunCommandPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
