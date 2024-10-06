package me.jtech.redstonecomptools;

import me.jtech.redstonecomptools.commands.*;
import me.jtech.redstonecomptools.networking.GiveItemPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Redstonecomptools implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("redstonecomptools");

    @Override
    public void onInitialize() {
        LOGGER.info("Registering Commands...");
        CalculateCommand.registerCommand();
        ReadBinCommand.registerCommand();
        WriteBinCommand.registerCommand();
        BitmapPrinterCommand.registerCommand();
        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of("fabric-docs", "block_pos"),
                BlockPosArgumentType.class,
                ConstantArgumentSerializer.of(BlockPosArgumentType::new)
        );


        LOGGER.info("Registering Packets...");

        PayloadTypeRegistry.playC2S().register(GiveItemPayload.ID, GiveItemPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GiveItemPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                PlayerEntity player = context.player();
                int slot = player.getInventory().getEmptySlot();
                player.getInventory().insertStack(slot, payload.item());
                player.getInventory().selectedSlot = slot;
            });
        }));
    }
}