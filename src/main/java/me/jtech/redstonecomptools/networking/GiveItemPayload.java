package me.jtech.redstonecomptools.networking;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record GiveItemPayload(ItemStack item) implements CustomPayload {
    public static final Id<GiveItemPayload> ID = new Id<>(NetworkingPackets.GIVE_ITEM_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, GiveItemPayload> CODEC = PacketCodec.tuple(ItemStack.PACKET_CODEC, GiveItemPayload::item, GiveItemPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
