package me.jtech.redstonecomptools.networking.payloads.c2s;

import me.jtech.redstonecomptools.networking.NetworkingPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SetItemPayload(ItemStack item, int slot) implements CustomPayload {
    public static final Id<SetItemPayload> ID = new Id<>(NetworkingPackets.SET_ITEM_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, SetItemPayload> CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC, SetItemPayload::item,
            PacketCodecs.INTEGER, SetItemPayload::slot,
            SetItemPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
