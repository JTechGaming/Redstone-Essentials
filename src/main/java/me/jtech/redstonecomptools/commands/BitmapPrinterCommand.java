package me.jtech.redstonecomptools.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstonecomptools.utility.Pair;
import me.jtech.redstonecomptools.utility.SelectionHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BitmapPrinterCommand { // TODO comment all this
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("pb")
                    .executes(BitmapPrinterCommand::noArgs)
                    .then(CommandManager.argument("file_path", StringArgumentType.string())
                            .executes(BitmapPrinterCommand::noArgs)
                            .then(CommandManager.argument("screen_width", IntegerArgumentType.integer())
                                    .executes(BitmapPrinterCommand::noArgs)
                                    .then(CommandManager.argument("screen_height", IntegerArgumentType.integer())
                                            .executes(BitmapPrinterCommand::noArgs)
                                            .then(CommandManager.argument("interval", IntegerArgumentType.integer())
                                                    .executes(BitmapPrinterCommand::noArgs)
                                                    .then(CommandManager.argument("offset", IntegerArgumentType.integer())
                                                            .executes(BitmapPrinterCommand::noArgs)
                                                            .then(CommandManager.argument("c1xpos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                    .then(CommandManager.argument("c1xpos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                            .then(CommandManager.argument("c1ypos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                    .then(CommandManager.argument("c1ypos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                                            .then(CommandManager.argument("c2xpos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                                    .then(CommandManager.argument("c2xpos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                                                            .then(CommandManager.argument("c2ypos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                                                    .then(CommandManager.argument("c2ypos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                                                                            .then(CommandManager.argument("c3xpos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                                                                    .then(CommandManager.argument("c3xpos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                                                                                            .then(CommandManager.argument("c3ypos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                                                                                    .then(CommandManager.argument("c3ypos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                                                            .executes(BitmapPrinterCommand::executeCommand)))))))))))))))))));
            dispatcher.register(CommandManager.literal("print_bitmap")
                    .executes(BitmapPrinterCommand::noArgs)
                    .then(CommandManager.argument("file_path", StringArgumentType.string())
                            .executes(BitmapPrinterCommand::noArgs)
                            .then(CommandManager.argument("screen_width", IntegerArgumentType.integer())
                                    .executes(BitmapPrinterCommand::noArgs)
                                    .then(CommandManager.argument("screen_height", IntegerArgumentType.integer())
                                            .executes(BitmapPrinterCommand::noArgs)
                                            .then(CommandManager.argument("interval", IntegerArgumentType.integer())
                                                    .executes(BitmapPrinterCommand::noArgs)
                                                    .then(CommandManager.argument("offset", IntegerArgumentType.integer())
                                                            .executes(BitmapPrinterCommand::noArgs)
                                                            .then(CommandManager.argument("c1xpos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                    .then(CommandManager.argument("c1xpos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                            .then(CommandManager.argument("c1ypos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                    .then(CommandManager.argument("c1ypos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                                            .then(CommandManager.argument("c2xpos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                                    .then(CommandManager.argument("c2xpos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                                                            .then(CommandManager.argument("c2ypos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                                                    .then(CommandManager.argument("c2ypos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                                                                            .then(CommandManager.argument("c3xpos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                                                                    .then(CommandManager.argument("c3xpos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                                                                                            .then(CommandManager.argument("c3ypos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                                                                                    .then(CommandManager.argument("c3ypos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                                                                            .executes(BitmapPrinterCommand::executeCommand)))))))))))))))))));
        });
    }

    private static int noArgs(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("You need to provide more arguments!"), false);
        return 1;
    }

    private static int executeCommand(CommandContext<ServerCommandSource> context) {
        String filePath = context.getArgument("file_path", String.class);
        int width = context.getArgument("screen_width", Integer.class);
        int height = context.getArgument("screen_height", Integer.class);
        int interval = context.getArgument("interval", Integer.class);
        int offset = context.getArgument("offset", Integer.class);
        BlockPos channel1xPos1 = context.getArgument("c1xpos1", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel1xPos2 = context.getArgument("c1xpos2", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel1yPos1 = context.getArgument("c1ypos1", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel1yPos2 = context.getArgument("c1ypos2", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel2xPos1 = context.getArgument("c2xpos1", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel2xPos2 = context.getArgument("c2xpos2", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel2yPos1 = context.getArgument("c2ypos1", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel2yPos2 = context.getArgument("c2ypos2", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel3xPos1 = context.getArgument("c3xpos1", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel3xPos2 = context.getArgument("c3xpos2", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel3yPos1 = context.getArgument("c3ypos1", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockPos channel3yPos2 = context.getArgument("c3ypos2", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());

        Path path = FabricLoader.getInstance().getConfigDir().resolve("redstonecomptools/bitmaps/").resolve(filePath);
        System.out.println("hi");
        System.out.println(path);

        System.out.println(path);
        File file = path.toFile();
        try {
            BufferedImage img = scale(ImageIO.read(file), width, height);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "bmp", baos);
            byte[] bytes = baos.toByteArray();

            int y = 1;
            List<Vec2f> writeLocations = new ArrayList<>();
            for (int i = 0; i < bytes.length; i++) {
                if (i > (width * y)) {
                    y++;
                }
                if (bytes[i] >= 0) {
                    int x = i / y;
                    writeLocations.add(new Vec2f(x, y));
                    System.out.println("x: " + x + " , y: " + y);
                }
            }

            AtomicBoolean isProcessing = new AtomicBoolean(true);

            AtomicInteger currentTick = new AtomicInteger();
            ServerTickEvents.END_SERVER_TICK.register((server) -> {
                if (isProcessing.get()) {
                    currentTick.getAndIncrement();
                    if (currentTick.get() == interval) {
                        currentTick.set(0);
                        executePlacement(
                                writeLocations,
                                new Pair<>(new Pair<>(channel1xPos1, channel1xPos2), new Pair<>(channel1yPos1, channel1yPos2)),
                                new Pair<>(new Pair<>(channel2xPos1, channel2xPos2), new Pair<>(channel2yPos1, channel2yPos2)),
                                new Pair<>(new Pair<>(channel3xPos1, channel3xPos2), new Pair<>(channel3yPos1, channel3yPos2)),
                                offset,
                                false, // TODO add invert direction arguments
                                context
                        );
                        if (writeLocations.size() <= 2) {
                            isProcessing.set(false);
                            context.getSource().sendFeedback(() -> Text.literal("Completed bitmap print!").withColor(3136078), false);
                            return;
                        }
                        writeLocations.remove(0);
                        writeLocations.remove(1);
                        writeLocations.remove(2);
                    }
                } // Test case: /print_bitmap "Test.bmp" 80 64 20 1 -57 68 -9 -57 68 -2 -57 72 -9 -57 72 -2 -57 68 1 -57 68 8 -57 72 1 -57 72 8 -57 68 10 -57 68 17 -57 72 10 -57 72 17
            });
        } catch (IOException e) {
            System.out.println(e);
        }

        return 0;
    }

    private static void executePlacement(List<Vec2f> writeLocations, Pair<Pair<BlockPos, BlockPos>, Pair<BlockPos, BlockPos>> byteLoc1, Pair<Pair<BlockPos, BlockPos>, Pair<BlockPos, BlockPos>> byteLoc2, Pair<Pair<BlockPos, BlockPos>, Pair<BlockPos, BlockPos>> byteLoc3, int offset, boolean invertDirection, CommandContext<ServerCommandSource> context) {
        Pair<Pair<BlockPos, BlockPos>, Pair<BlockPos, BlockPos>> currentByte = byteLoc1;
        for (int i = 0; i < 3; i++) {
            SelectionHelper selection = new SelectionHelper(currentByte.getFirst().getFirst(), currentByte.getFirst().getSecond(), invertDirection);
            selection.writeData(context.getSource().getWorld(), (int) writeLocations.get(i).x, offset, SelectionHelper.Mode.WRITE);
            selection = new SelectionHelper(currentByte.getSecond().getFirst(), currentByte.getSecond().getSecond(), invertDirection);
            selection.writeData(context.getSource().getWorld(), (int) writeLocations.get(i).y, offset, SelectionHelper.Mode.WRITE);
            if (i == 1) {
                currentByte = byteLoc2;
                continue;
            }
            currentByte = byteLoc3;
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
