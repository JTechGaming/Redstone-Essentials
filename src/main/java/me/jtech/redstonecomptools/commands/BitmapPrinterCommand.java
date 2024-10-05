package me.jtech.redstonecomptools.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class BitmapPrinterCommand {
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("pb")
                    .executes(BitmapPrinterCommand::noArgs)
                    .then(CommandManager.argument("file_path", StringArgumentType.string())
                            .executes(BitmapPrinterCommand::noArgs)
                            .then(CommandManager.argument("screen_width", IntegerArgumentType.integer()))
                            .executes(BitmapPrinterCommand::noArgs)
                            .then(CommandManager.argument("screen_height", IntegerArgumentType.integer()))
                            .executes(BitmapPrinterCommand::noArgs)
                            .then(CommandManager.argument("channels", IntegerArgumentType.integer()))
                            .executes(BitmapPrinterCommand::noArgs)
                            .then(CommandManager.argument("interval", IntegerArgumentType.integer()))
                            .executes(BitmapPrinterCommand::executeCommand)));
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
                                                    .then(CommandManager.argument("c1pos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                            .executes(BitmapPrinterCommand::noArgs)
                                                            .then(CommandManager.argument("c1pos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                    .then(CommandManager.argument("c2pos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                            .then(CommandManager.argument("c2pos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                    .executes(BitmapPrinterCommand::noArgs)
                                                                                    .then(CommandManager.argument("c3pos1", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                            .executes(BitmapPrinterCommand::noArgs)
                                                                                            .then(CommandManager.argument("c3pos2", new net.minecraft.command.argument.BlockPosArgumentType())
                                                                                                    .executes(BitmapPrinterCommand::executeCommand))))))))))));
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
        DefaultPosArgument channel1Pos1 = context.getArgument("c1pos1", DefaultPosArgument.class);
        DefaultPosArgument channel1Pos2 = context.getArgument("c1pos2", DefaultPosArgument.class);
        DefaultPosArgument channel2Pos1 = context.getArgument("c2pos1", DefaultPosArgument.class);
        DefaultPosArgument channel2Pos2 = context.getArgument("c2pos2", DefaultPosArgument.class);
        DefaultPosArgument channel3Pos1 = context.getArgument("c3pos1", DefaultPosArgument.class);
        DefaultPosArgument channel3Pos2 = context.getArgument("c3pos2", DefaultPosArgument.class);

        Path path = FabricLoader.getInstance().getConfigDir().resolve(filePath);
        String binWidth = Integer.toBinaryString(width);
        String binHeight = Integer.toBinaryString(height);

        System.out.println(path);
        File file = path.toFile();
        try {
            BufferedImage img = scale(ImageIO.read(file), width, height);

            ImageIO.write(img, "bmp", new File("out.bmp"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "bmp", baos);
            byte[] bytes = baos.toByteArray();

            int y = 1;

            String out = "";

            for (int i=0; i<bytes.length; i++) {
                if (i > (width*y)) {
                    y++;
                }
                if (bytes[i] >= 0) {
                    int x = i / y;
                }
            }

            System.out.println(out);
        } catch (IOException e) {
            System.out.println(e);
        }

        return 0;
    }

    public static BufferedImage scale(BufferedImage src, int w, int h)
    {
        BufferedImage img =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int x, y;
        int ww = src.getWidth();
        int hh = src.getHeight();
        int[] ys = new int[h];
        for (y = 0; y < h; y++)
            ys[y] = y * hh / h;
        for (x = 0; x < w; x++) {
            int newX = x * ww / w;
            for (y = 0; y < h; y++) {
                int col = src.getRGB(newX, ys[y]);
                img.setRGB(x, y, col);
            }
        }
        return img;
    }
}
