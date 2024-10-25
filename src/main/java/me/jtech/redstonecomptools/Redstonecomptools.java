package me.jtech.redstonecomptools;

import com.mojang.brigadier.ParseResults;
import eu.midnightdust.lib.config.MidnightConfig;
import me.jtech.redstonecomptools.commands.*;
import me.jtech.redstonecomptools.IO.Config;
import me.jtech.redstonecomptools.networking.payloads.c2s.*;
import me.jtech.redstonecomptools.networking.payloads.s2c.ClientSetBlockPayload;
import me.jtech.redstonecomptools.networking.payloads.s2c.ClientsRenderPingPayload;
import me.jtech.redstonecomptools.networking.payloads.s2c.FinishBitmapPrintPayload;
import me.jtech.redstonecomptools.networking.payloads.s2c.OpenScreenPayload;
import me.jtech.redstonecomptools.utility.IClientSelectionContext;
import me.jtech.redstonecomptools.utility.SelectionContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.ChunkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;

public class Redstonecomptools implements ModInitializer, IClientSelectionContext { // TODO comment this

    public static final Logger LOGGER = LoggerFactory.getLogger("redstonecomptools");
    public static final String MOD_ID = "redstonecomptools";

    public static boolean shouldApplyButtonStyle = false;

    private static Redstonecomptools instance;

    public int DEFAULT_CONTEXT = SelectionContext.register(this);

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("Starting RedstoneCompTools v1.0.6-BETA");
        try {
            Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("redstonecomptools/bitmaps/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Registering Commands...");
        CalculateCommand.registerCommand();
        ReadBinCommand.registerCommand();
        WriteBinCommand.registerCommand();
        BitmapPrinterCommand.registerCommand();
        ListBitmapsCommand.registerCommand();

        LOGGER.info("Setting up Server-Side Packets...");

        PayloadTypeRegistry.playC2S().register(GiveItemPayload.ID, GiveItemPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetItemPayload.ID, SetItemPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RunCommandPayload.ID, RunCommandPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerSendClientPingPayload.ID, ServerSendClientPingPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetBlockPayload.ID, SetBlockPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ClientsRenderPingPayload.ID, ClientsRenderPingPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenScreenPayload.ID, OpenScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FinishBitmapPrintPayload.ID, FinishBitmapPrintPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientSetBlockPayload.ID, ClientSetBlockPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GiveItemPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                PlayerEntity player = context.player();
                int slot = player.getInventory().getEmptySlot();
                player.getInventory().insertStack(slot, payload.item());
                player.getInventory().selectedSlot = slot;
                player.getInventory().updateItems();
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

        ServerPlayNetworking.registerGlobalReceiver(SetBlockPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                PlayerEntity placer = context.player();
                Item returnItem = Registries.ITEM.get(Identifier.ofVanilla(payload.blockName().toLowerCase()));
                boolean dropItems = placer.getWorld().getGameRules().getBoolean(GameRules.DO_TILE_DROPS);
                placer.getWorld().getGameRules().get(GameRules.DO_TILE_DROPS).set(false, placer.getWorld().getServer());
                BlockState state = null;
                if (returnItem.equals(Items.REDSTONE)) {
                    state = Blocks.REDSTONE_WIRE.getPlacementState(new AutomaticItemPlacementContext(placer.getWorld(), payload.blockPos(), Direction.getFacing(payload.blockPos().toCenterPos()), Items.REDSTONE.getDefaultStack(), Direction.UP));
                } else if (returnItem.equals(Items.AIR)) {
                    state = Blocks.AIR.getDefaultState();
                } else if (BlockItem.BLOCK_ITEMS.containsValue(returnItem)){
                    for (Block block : BlockItem.BLOCK_ITEMS.keySet()) {
                        if (BlockItem.BLOCK_ITEMS.get(block).equals(returnItem)) {
                            state = block.getPlacementState(new AutomaticItemPlacementContext(placer.getWorld(), payload.blockPos(), Direction.getFacing(payload.blockPos().toCenterPos()), returnItem.getDefaultStack(), Direction.UP));
                        }
                    }
                }
                if (state != null) {
                    if (!placer.getWorld().isChunkLoaded(payload.blockPos().getX() >> 4, payload.blockPos().getY() >> 4)) { // Check if loaded
                        placer.getWorld().getChunk(payload.blockPos().getX() >> 4, payload.blockPos().getY() >> 4, ChunkStatus.FULL, true); // If not loaded, load the chunk
                    }

                    placer.getWorld().setBlockState(payload.blockPos(), state); // Set the block
                }

                placer.getWorld().getGameRules().get(GameRules.DO_TILE_DROPS).set(dropItems, placer.getWorld().getServer());
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
                    ServerPlayerLabelStorage.addPlayerRTBO(player, new SelectionData(payload.blockPos(), Color.getHSBColor(payload.rgb().x, payload.rgb().y, payload.rgb().z), new Vec3i((int) payload.size().x, (int) payload.size().y, (int) payload.size().z), payload.label(), payload.isRTBOOverlay()));
                    if (player != context.player()) {
                        ServerPlayNetworking.send(player, new ClientsRenderPingPayload(payload.blockPos(), payload.rgb(), payload.size(), payload.isSelectionOverlay(), payload.isRTBOOverlay(), payload.label()));
                    }
                }
            });
        }));


        MidnightConfig.init(MOD_ID, Config.class);
    }

    @Override
    public void recall(BlockPos blockPos, Color color, Vec3i size, int id, boolean wasModified) {

    }

    public static Redstonecomptools getInstance() {
        return instance;
    }

    public static void setInstance(Redstonecomptools instance) {
        Redstonecomptools.instance = instance;
    }
}