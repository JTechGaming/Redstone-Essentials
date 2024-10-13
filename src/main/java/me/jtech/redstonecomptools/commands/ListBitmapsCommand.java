package me.jtech.redstonecomptools.commands;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileOwnerAttributeView;

public class ListBitmapsCommand {
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("list_bitmaps")
                    .executes(ListBitmapsCommand::executeCommand));
            dispatcher.register(CommandManager.literal("lb")
                    .executes(ListBitmapsCommand::executeCommand));
        });
    }

    private static int executeCommand(CommandContext<ServerCommandSource> context) {
        String bitmapList = "";
        Path folderPath = FabricLoader.getInstance().getConfigDir().resolve("/redstonecomptools/bitmaps/");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath.toAbsolutePath())) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    bitmapList = bitmapList.concat(
                            " ||  Name: " + path.getFileName().toString() +
                                    "  |  Author: " + Files.getFileAttributeView(path, FileOwnerAttributeView.class).getOwner().getName() +
                                    "\n"
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String finalBitmapList = bitmapList;
        context.getSource().sendFeedback(() -> Text.literal(finalBitmapList).withColor(8251903), false);
        return 0;
    }
}
