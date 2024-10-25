package me.jtech.redstonecomptools.commands;

import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstonecomptools.Redstonecomptools;
import me.jtech.redstonecomptools.SelectionData;
import me.jtech.redstonecomptools.IO.Config;
import me.jtech.redstonecomptools.networking.payloads.s2c.FinishBitmapPrintPayload;
import me.jtech.redstonecomptools.networking.payloads.s2c.OpenScreenPayload;
import me.jtech.redstonecomptools.utility.Pair;
import me.jtech.redstonecomptools.utility.SelectionHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BitmapPrinterCommand { // TODO comment all this
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("pb")
                    .executes(BitmapPrinterCommand::executeCommand));
            dispatcher.register(CommandManager.literal("print_bitmap")
                    .executes(BitmapPrinterCommand::executeCommand));
        });
    }

    private static int noArgs(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("You need to provide more arguments!"), false);
        return 1;
    }

    private static int executeCommand(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        assert player != null;
        ServerPlayNetworking.send(player, new OpenScreenPayload(0)); // Open Bitmap Printer Screen
        return 0;
    }

    public static void finaliseExecution(String filePath, List<SelectionData> selectionList, int width, int height, int interval, int channels, World world, PlayerEntity clientPlayer) {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("redstonecomptools/bitmaps/").resolve(filePath);
        List<Vec2f> writeLocations = new ArrayList<>();
        File file = path.toFile();
        try {
            BufferedImage scaledImg = scale(ImageIO.read(file), width, height);
            for (int y = 0; y < scaledImg.getHeight(); y++) {
                for (int x = 0; x < scaledImg.getWidth(); x++) {
                    int pixel = scaledImg.getRGB(x, y);
                    Color color = new Color(pixel, true);
                    int addent = color.getRed() + color.getGreen() + color.getBlue();
                    if (addent <= 0) {
                        writeLocations.add(new Vec2f(scaledImg.getWidth()-x, scaledImg.getHeight()-y));
                    }
                }
            }
            AtomicBoolean isProcessing = new AtomicBoolean(true);
            AtomicInteger currentTick = new AtomicInteger();
            AtomicInteger currentOffset = new AtomicInteger();
            ServerTickEvents.END_SERVER_TICK.register((server) -> {
                if (isProcessing.get()) {
                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(clientPlayer.getUuid());
                    currentTick.getAndIncrement();
                    if (currentTick.get() == interval) {
                        currentTick.set(0);
                        if (writeLocations.size()-1 <= currentOffset.get()+channels) {
                            isProcessing.set(false);

                            if (Config.bitmap_reset_on_finish) {
                                for (int i = 0; i < channels; i++) { // Make sure all channels x and y are reset to 0
                                    if (writeLocations.size() - 1 <= i + currentOffset.get()) {
                                        currentOffset.incrementAndGet();
                                        return;
                                    }
                                    Pair<SelectionData, SelectionData> currentByte = new Pair<>(selectionList.get(i), selectionList.get(i + 1));

                                    SelectionHelper selection = new SelectionHelper(currentByte.getFirst().getBlockPos(), currentByte.getFirst().getBlockPos().add(currentByte.getFirst().getSize().subtract(new Vec3i(1, 1, 1))), currentByte.getFirst().isInverted());
                                    selection.writeData(world, 0, currentByte.getFirst().getOffset(), SelectionHelper.Mode.WRITE, player);
                                    selection = new SelectionHelper(currentByte.getSecond().getBlockPos(), currentByte.getSecond().getBlockPos().add(currentByte.getSecond().getSize().subtract(new Vec3i(1, 1, 1))), currentByte.getSecond().isInverted());
                                    selection.writeData(world, 0, currentByte.getSecond().getOffset(), SelectionHelper.Mode.WRITE, player);
                                }
                            }

                            if (player!=null) {
                                ServerPlayNetworking.send(player, new FinishBitmapPrintPayload(true));
                            } else {
                                Redstonecomptools.LOGGER.error("Player doesnt exist");
                            }
                            return;
                        }
                        executePlacement(selectionList, writeLocations, channels, world, currentOffset, player);
                        currentOffset.incrementAndGet();
                    }
                }
            });
        } catch (IOException e) {
            Redstonecomptools.LOGGER.error(String.valueOf(e));
        }
    }

    private static void executePlacement(List<SelectionData> selectionList, List<Vec2f> writeLocations, int channels, World world, AtomicInteger currentOffset, ServerPlayerEntity player) {
        for (int i=0; i<channels; i++) {
            if (writeLocations.size()-1 <= i+currentOffset.get()) {
                currentOffset.incrementAndGet();
                return;
            }
            Pair<SelectionData, SelectionData> currentByte = new Pair<>(selectionList.get(i), selectionList.get(i+1));

            SelectionHelper selection = new SelectionHelper(currentByte.getFirst().getBlockPos(), currentByte.getFirst().getBlockPos().add(currentByte.getFirst().getSize().subtract(new Vec3i(1, 1, 1))), currentByte.getFirst().isInverted());
            selection.writeData(world, (int) writeLocations.get(i+currentOffset.get()).x, currentByte.getFirst().getOffset(), SelectionHelper.Mode.WRITE, player);
            selection = new SelectionHelper(currentByte.getSecond().getBlockPos(), currentByte.getSecond().getBlockPos().add(currentByte.getSecond().getSize().subtract(new Vec3i(1, 1, 1))), currentByte.getSecond().isInverted());
            selection.writeData(world, (int) writeLocations.get(i+currentOffset.get()).y, currentByte.getSecond().getOffset(), SelectionHelper.Mode.WRITE, player);
        }
    }

    public static BufferedImage scale(BufferedImage src, int width, int height) {
        BufferedImage img =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int x, y;
        int sourceWidth = src.getWidth();
        int sourceHeight = src.getHeight();
        int[] ys = new int[height];
        for (y = 0; y < height; y++)
            ys[y] = y * sourceHeight / height;
        for (x = 0; x < width; x++) {
            int newX = x * sourceWidth / width;
            for (y = 0; y < height; y++) {
                int col = src.getRGB(newX, ys[y]);
                img.setRGB(x, y, col);
            }
        }
        return img;
    }
}
