package me.jtech.redstonecomptools;

import com.mojang.brigadier.ParseResults;
import eu.midnightdust.lib.config.MidnightConfig;
import me.jtech.redstonecomptools.commands.*;
import me.jtech.redstonecomptools.config.Config;
import me.jtech.redstonecomptools.networking.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Redstonecomptools implements ModInitializer { // TODO comment this

    public static final Logger LOGGER = LoggerFactory.getLogger("redstonecomptools");
    public static final String MOD_ID = "redstonecomptools";

    public static boolean shouldApplyButtonStyle = false;

    @Override
    public void onInitialize() {
//        try {
//            Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("/redstonecomptools/bitmaps/"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        LOGGER.info("Registering Commands...");
        CalculateCommand.registerCommand();
        ReadBinCommand.registerCommand();
        WriteBinCommand.registerCommand();
        BitmapPrinterCommand.registerCommand();
        ListBitmapsCommand.registerCommand();

        LOGGER.info("Registering Packets...");

        PayloadTypeRegistry.playC2S().register(GiveItemPayload.ID, GiveItemPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetItemPayload.ID, SetItemPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RunCommandPayload.ID, RunCommandPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerSendClientPingPayload.ID, ServerSendClientPingPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ClientsRenderPingPayload.ID, ClientsRenderPingPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GiveItemPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                PlayerEntity player = context.player();
                int slot = player.getInventory().getEmptySlot();
                player.getInventory().insertStack(slot, payload.item());
                player.getInventory().selectedSlot = slot;
                player.getInventory().updateItems();
                System.out.println("received packet");
            });
        }));

        ServerPlayNetworking.registerGlobalReceiver(SetItemPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                PlayerEntity player = context.player();
                player.getInventory().removeStack(payload.slot());
                player.getInventory().insertStack(payload.slot(), payload.item());
                player.getInventory().updateItems();
            });
        }));

        ServerPlayNetworking.registerGlobalReceiver(RunCommandPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                String command = payload.command();
                command = command.replace("/", "");

                ServerCommandSource commandSource = context.player().getCommandSource();

                // Parse the command string into ParseResults
                ParseResults<ServerCommandSource> parseResults = context.server().getCommandManager().getDispatcher().parse(command, commandSource);

                context.server().getCommandManager().execute(parseResults, command);
            });
        }));

        ServerPlayNetworking.registerGlobalReceiver(ServerSendClientPingPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                    if (player != context.player()) {
                        ServerPlayNetworking.send(player, new ClientsRenderPingPayload(payload.blockPos(), payload.color(), payload.size()));
                    }
                }
            });
        }));


        MidnightConfig.init(MOD_ID, Config.class);
    }
}