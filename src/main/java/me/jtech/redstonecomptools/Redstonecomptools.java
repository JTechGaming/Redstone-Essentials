package me.jtech.redstonecomptools;

import me.jtech.redstonecomptools.commands.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
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
    }
}
// Ctrl + Shift + F9