package me.jtech.redstone_essentials;

import eu.midnightdust.lib.config.MidnightConfig;
import me.jtech.redstone_essentials.commands.*;
import me.jtech.redstone_essentials.IO.Config;
import me.jtech.redstone_essentials.networking.InfoPackets;
import me.jtech.redstone_essentials.networking.payloads.c2s.*;
import me.jtech.redstone_essentials.networking.payloads.s2c.*;
import me.jtech.redstone_essentials.utility.IClientSelectionContext;
import me.jtech.redstone_essentials.utility.SelectionContext;
import me.jtech.redstone_essentials.utility.SelectionHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Redstone_Essentials implements ModInitializer, IClientSelectionContext { // TODO comment this

    public static final String MOD_VERSION = "1.0.9+d188";

    public static final Logger LOGGER = LoggerFactory.getLogger("redstone_essentials");
    public static final String MOD_ID = "redstone_essentials";

    public static boolean shouldApplyButtonStyle = false;

    private static Redstone_Essentials instance;

    public int DEFAULT_CONTEXT = SelectionContext.register(this);

    private final Path bitmapsPath = FabricLoader.getInstance().getConfigDir().resolve("redstone_essentials/bitmaps/");

    public static List<ServerPlayerEntity> outdatedClients = new ArrayList<>();

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("Starting Redstone Essentials v" + MOD_VERSION);
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
                    state = Blocks.REDSTONE_WIRE.getDefaultState();
                } else if (returnItem.equals(Items.AIR)) {
                    state = Blocks.AIR.getDefaultState();
                } else if (BlockItem.BLOCK_ITEMS.containsValue(returnItem)) {
                    for (Block block : BlockItem.BLOCK_ITEMS.keySet()) {
                        if (BlockItem.BLOCK_ITEMS.get(block).equals(returnItem)) {
                            state = block.getPlacementState(new AutomaticItemPlacementContext(placer.getWorld(), payload.blockPos(), Direction.UP, returnItem.getDefaultStack(), Direction.UP));
                        }
                    }
                }


                if (!payload.supportBlockName().equalsIgnoreCase("none") && !payload.supportBlockName().isEmpty()) {
                    Item supportBlockItem = Registries.ITEM.get(Identifier.ofVanilla(payload.supportBlockName().toLowerCase()));

                    BlockState supportState = null;
                    for (Block block : BlockItem.BLOCK_ITEMS.keySet()) {
                        if (BlockItem.BLOCK_ITEMS.get(block).equals(supportBlockItem)) {
                            supportState = block.getDefaultState();
                        }
                    }

                    setBlock(context, world, supportState, new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));
                }

                setBlock(context, world, state, pos);

                placer.getWorld().getGameRules().get(GameRules.DO_TILE_DROPS).set(dropItems, placer.getWorld().getServer());
            });
        }));

        ServerPlayNetworking.registerGlobalReceiver(ServerSendClientPingPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                    ServerPlayerLabelStorage.addPlayerRTBO(player, new SelectionData(payload.blockPos(), Color.getHSBColor(payload.rgb().x, payload.rgb().y, payload.rgb().z), new Vec3i((int) payload.size().x, (int) payload.size().y, (int) payload.size().z), payload.label(), payload.isRTBOOverlay(), context.player().getName().getString()));
                    if (player != context.player() && !Redstone_Essentials.outdatedClients.contains(player)) {
                        ServerPlayNetworking.send(player, new ClientsRenderPingPayload(payload.blockPos(), payload.rgb(), payload.size(), new Vector3f(payload.isSelectionOverlay() ? 1 : 0, payload.isRTBOOverlay() ? 1 : 0, 0), payload.label(), context.player().getName().getString()));
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
                    for (int i = 0; i < bitmaps.size(); i++) {
                        if (!Redstone_Essentials.outdatedClients.contains(context.player())) {
                            ServerPlayNetworking.send(context.player(), new ServerSendBitmapPayload(bitmaps.get(i), (i == (bitmaps.size() - 1))));
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(String.valueOf(e));
                }
            });
        }));

        ServerPlayNetworking.registerGlobalReceiver(C2SInfoPacket.ID, ((payload, context) -> {
            context.server().execute(() -> {
                InfoPackets.C2S infoID = InfoPackets.getC2SEnum(payload.infoID());
                switch (infoID) {
                    case FINALISE_BITMAP -> {
                        String width = payload.flag2();
                        String height = width.substring(width.indexOf("♅") + 1);
                        String interval = height.substring(height.indexOf("♅") + 1);
                        String channels = interval.substring(interval.indexOf("♅") + 1);
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
                    case FORCE_NEIGHBOUR_UPDATES -> {
                        String blockPayload = payload.flag1();
                        String x = payload.flag2();
                        String y = x.substring(x.indexOf("♅") + 1);
                        String z = y.substring(y.indexOf("♅") + 1);

                        BlockPos blockPos = new BlockPos(
                                Integer.parseInt(x.substring(0, x.indexOf("♅"))),
                                Integer.parseInt(y.substring(0, y.indexOf("♅"))),
                                Integer.parseInt(z.substring(0, z.indexOf("♅")))
                        );

                        ServerWorld world = context.player().getServerWorld();

                        Item item = Registries.ITEM.get(Identifier.ofVanilla(blockPayload));
                        Block block = Block.getBlockFromItem(item);
                        if (block != Blocks.REDSTONE_WIRE) {
                            return;
                        }
                        BlockState currentState = world.getBlockState(blockPos);
                        Direction currentFacing = Direction.UP;
                        Direction currentSide = Direction.UP;
                        if (currentState.getProperties().contains(Properties.FACING) && currentState.getProperties().contains(Properties.HORIZONTAL_FACING)) {
                            currentFacing = currentState.get(Properties.FACING);
                            currentSide = currentState.get(Properties.HORIZONTAL_FACING);
                        }
                        // TODO&1.1: fix this not working with observers and breaking tall grass and water
                        BlockState state = block.getPlacementState(new AutomaticItemPlacementContext(world, blockPos, currentFacing, item.getDefaultStack(), currentSide));
                        world.setBlockState(blockPos, state, Block.NOTIFY_ALL);
                    }
                    case ASK_SERVER_MOD_VERSION -> {
                        // TODO&1.2: setup a proper versioning system which keeps track of what systems are compatible with what versions
//                        if (!payload.flag1().equalsIgnoreCase(MOD_VERSION)) {
//                            Redstone_Essentials.outdatedClients.add(context.player());
//                            return;
//                        }
                        ServerPlayNetworking.send(context.player(), new S2CInfoPacket(InfoPackets.getInt(InfoPackets.S2C.CLIENT_RECEIVE_SERVER_VERSION), MOD_VERSION, "", "", new ArrayList<>()));
                        //ServerPlayNetworking.send(context.player(), new S2CInfoPacket(InfoPackets.getInt(InfoPackets.S2C.CLIENT_RECEIVE_SERVER_VERSION), payload.flag1(), "", "", new ArrayList<>()));
                    }
                    case RETURN_RW_BIN -> {
                        String mode = payload.flag1();
                        SelectionHelper selectionHelper = new SelectionHelper(payload.selections().get(0).blockPos, payload.selections().get(0).blockPos.add(payload.selections().get(0).size), false);
                        if (mode.equalsIgnoreCase("read")) {
                            context.player().sendMessage(Text.literal(selectionHelper.readData(context.player().getServerWorld(), 1) + ""));
                            return;
                        }
                        // TODO&1.1: make the data and offset configurable
                        selectionHelper.writeData(context.player().getServerWorld(), 3, 1, context.player());
                    }
                    case CLEAR_PINGS -> {
                        LOGGER.info("Clearing pings for player " + context.player().getName().getString());
                        for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                            if (player != context.player() && !Redstone_Essentials.outdatedClients.contains(player)) {
                                LOGGER.info("Sending clear pings to " + player.getName().getString());
                                ServerPlayNetworking.send(player, new S2CInfoPacket(InfoPackets.getInt(InfoPackets.S2C.CLEAR_PINGS), context.player().getName().getString(), "", "", payload.selections()));
                            }
                        }
                    }
                    case SEND_PINGS_TO_NEW_CLIENT -> {
                        ServerPlayerEntity player = payload.flag1().isEmpty() ? null : context.server().getPlayerManager().getPlayer(payload.flag1());
                        if (player == null) return;
                        for (SelectionData selection : payload.selections()) {
                            ServerPlayNetworking.send(player, new ClientsRenderPingPayload(
                                    selection.blockPos,
                                    new Vector3f(selection.color.getRed() / 255f, selection.color.getGreen() / 255f, selection.color.getBlue() / 255f),
                                    new Vector3f(selection.size.getX(), selection.size.getY(), selection.size.getZ()),
                                    new Vector3f(0, selection.isRTBO ? 1 : 0, 0),
                                    selection.label,
                                    context.player().getName().getString())
                            );
                            LOGGER.info("Sent ping to " + context.player().getName().getString());
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + payload.infoID());
                }
            });
        }));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player != sender && !Redstone_Essentials.outdatedClients.contains(player)) {
                    ServerPlayerEntity playerEntity = handler.player;
                    ServerPlayNetworking.send(player, new S2CInfoPacket(InfoPackets.getInt(InfoPackets.S2C.SEND_PINGS_TO_NEW_CLIENT), playerEntity.getName().getString(), "", "", new ArrayList<>()));
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            Redstone_Essentials.outdatedClients.remove(handler.player);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player != handler.player && !Redstone_Essentials.outdatedClients.contains(player)) {
                    ServerPlayNetworking.send(player, new S2CInfoPacket(InfoPackets.getInt(InfoPackets.S2C.CLEAR_PINGS), handler.player.getName().getString(), "", "", new ArrayList<>()));
                }
            }
        });

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

    public static void setBlock(ServerPlayNetworking.Context context, World world, BlockState state, BlockPos pos) {
        ServerWorld serverWorld = context.server().getWorld(world.getRegistryKey());
        if (serverWorld != null) {
            serverWorld.setBlockState(pos, state, Block.NOTIFY_ALL);

            serverWorld.updateNeighbors(pos, state.getBlock());
        }
    }
}