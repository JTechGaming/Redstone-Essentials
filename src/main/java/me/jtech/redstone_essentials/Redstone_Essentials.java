package me.jtech.redstone_essentials;

import com.mojang.brigadier.ParseResults;
import eu.midnightdust.lib.config.MidnightConfig;
import me.jtech.redstone_essentials.commands.*;
import me.jtech.redstone_essentials.IO.Config;
import me.jtech.redstone_essentials.networking.payloads.c2s.*;
import me.jtech.redstone_essentials.networking.payloads.s2c.*;
import me.jtech.redstone_essentials.utility.IClientSelectionContext;
import me.jtech.redstone_essentials.utility.SelectionContext;
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
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Redstone_Essentials implements ModInitializer, IClientSelectionContext { // TODO comment this

    public static final Logger LOGGER = LoggerFactory.getLogger("redstone_essentials");
    public static final String MOD_ID = "redstone_essentials";

    public static boolean shouldApplyButtonStyle = false;

    private static Redstone_Essentials instance;

    public int DEFAULT_CONTEXT = SelectionContext.register(this);

    private final Path bitmapsPath = FabricLoader.getInstance().getConfigDir().resolve("redstone_essentials/bitmaps/");

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("Starting Redstone Essentials v1.0.7-BETA");
        try {
            Files.createDirectories(bitmapsPath);
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
        PayloadTypeRegistry.playC2S().register(ClientGetServerBitmapsPayload.ID, ClientGetServerBitmapsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(C2SInfoPacket.ID, C2SInfoPacket.CODEC);

        PayloadTypeRegistry.playS2C().register(ClientsRenderPingPayload.ID, ClientsRenderPingPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenScreenPayload.ID, OpenScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FinishBitmapPrintPayload.ID, FinishBitmapPrintPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientSetBlockPayload.ID, ClientSetBlockPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ServerSendBitmapPayload.ID, ServerSendBitmapPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CInfoPacket.ID, S2CInfoPacket.CODEC);

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
                World world = placer.getWorld();
                BlockPos pos = payload.blockPos();
                Item returnItem = Registries.ITEM.get(Identifier.ofVanilla(payload.blockName().toLowerCase()));
                boolean dropItems = placer.getWorld().getGameRules().getBoolean(GameRules.DO_TILE_DROPS);
                placer.getWorld().getGameRules().get(GameRules.DO_TILE_DROPS).set(false, placer.getWorld().getServer());
                BlockState state = null;
                if (returnItem.equals(Items.REDSTONE)) {
                    ItemPlacementContext placementContext = new ItemPlacementContext(placer, Hand.MAIN_HAND, new ItemStack(Items.REDSTONE), new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
                    state = Blocks.REDSTONE_WIRE.getPlacementState(placementContext);
                } else if (returnItem.equals(Items.AIR)) {
                    state = Blocks.AIR.getDefaultState();
                } else if (BlockItem.BLOCK_ITEMS.containsValue(returnItem)){
                    for (Block block : BlockItem.BLOCK_ITEMS.keySet()) {
                        if (BlockItem.BLOCK_ITEMS.get(block).equals(returnItem)) {
                            state = block.getPlacementState(new AutomaticItemPlacementContext(placer.getWorld(), payload.blockPos(), Direction.UP, returnItem.getDefaultStack(), Direction.UP));
                        }
                    }
                }

                if (state != null && state.canPlaceAt(world, pos)) {
                    // Load chunk if not already loaded
                    if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                        world.getChunkManager().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
                    }

                    // Set block state and update surrounding neighbors
                    world.setBlockState(pos, state, 3);  // Flag 3: update client and notify neighbors
                    world.updateNeighborsAlways(pos, Blocks.REDSTONE_WIRE);
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

        ServerPlayNetworking.registerGlobalReceiver(ClientGetServerBitmapsPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                List<String> bitmaps;
                try (Stream<Path> stream = Files.list(bitmapsPath)) {
                    bitmaps = stream
                            .filter(file -> !Files.isDirectory(file))
                            .map(Path::getFileName)
                            .map(Path::toString)
                            .toList();
                    for (int i=0; i<bitmaps.size(); i++) {
                        ServerPlayNetworking.send(context.player(), new ServerSendBitmapPayload(bitmaps.get(i), (i==(bitmaps.size()-1))));
                    }
                } catch (IOException e) {
                    LOGGER.error(String.valueOf(e));
                }
            });
        }));

        ServerPlayNetworking.registerGlobalReceiver(C2SInfoPacket.ID, ((payload, context) -> {
            context.server().execute(() -> {
                switch (payload.infoID()) {
                    case 0 -> {
                        String width = payload.flag2();
                        String height = width.substring(width.indexOf("♅")+1);
                        String interval = height.substring(height.indexOf("♅")+1);
                        String channels = interval.substring(interval.indexOf("♅")+1);
                        BitmapPrinterCommand.finaliseExecution(
                            payload.flag1(),
                            payload.selections(),
                            Integer.parseInt(width.substring(0, width.indexOf("♅"))),
                            Integer.parseInt(height.substring(0, height.indexOf("♅"))),
                            Integer.parseInt(interval.substring(0, interval.indexOf("♅"))),
                            Integer.parseInt(channels),
                            context.player().getWorld(),
                            context.player());
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + payload.infoID());
                }
            });
        }));

        MidnightConfig.init(MOD_ID, Config.class);
    }

    @Override
    public void recall(BlockPos blockPos, Color color, Vec3i size, int id, boolean wasModified) {

    }

    public static Redstone_Essentials getInstance() {
        return instance;
    }

    public static void setInstance(Redstone_Essentials instance) {
        Redstone_Essentials.instance = instance;
    }
}