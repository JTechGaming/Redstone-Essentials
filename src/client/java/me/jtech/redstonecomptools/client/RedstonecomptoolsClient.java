package me.jtech.redstonecomptools.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstonecomptools.client.axiomExtensions.ServiceHelper;
import me.jtech.redstonecomptools.client.axiomExtensions.tools.forceNeighborUpdatesTool;
import me.jtech.redstonecomptools.client.keybinds.DynamicKeybindHandler;
import me.jtech.redstonecomptools.client.keybinds.DynamicKeybindProperties;
import me.jtech.redstonecomptools.client.qolTools.SignalStrengthGiver;
import me.jtech.redstonecomptools.client.rendering.BlockOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class RedstonecomptoolsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("redstonecomptools");
    public static final String MOD_ID = "redstonecomptools";

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initialising Registers...");
        AbilityManager.init();
        Abilities abilities = Abilities.getInstance();
        AbilityManager.initAbilities();

        DynamicKeybindHandler.loadKeybinds();

        LOGGER.info("Setting up QOL features...");
        // Setup keybinds for barrel and shulker giver
        SignalStrengthGiver.setupKeybinds();

        LOGGER.info("Registering axiom extension tools...");
        ServiceHelper.getToolRegistryService().register(new forceNeighborUpdatesTool());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SignalStrengthGiver.processBarrel();
            SignalStrengthGiver.processShulker();

            DynamicKeybindHandler.checkKeyPresses();
        });

        registerCommand();

        WorldRenderEvents.BLOCK_OUTLINE.register((worldRenderContext, blockOutlineContext) -> {
            BlockOverlay.renderAll(worldRenderContext.matrixStack(), worldRenderContext.consumers());
            return true;
        });
    }

    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("keybind")
                    .then(CommandManager.argument("name", StringArgumentType.string())
                            .then(CommandManager.argument("key", IntegerArgumentType.integer())
                            .then(CommandManager.argument("command", greedyString())
                            .executes(RedstonecomptoolsClient::executeCommand)))));
        });
    }

    private static int executeCommand(CommandContext<ServerCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        int key = IntegerArgumentType.getInteger(context, "key");
        String command = StringArgumentType.getString(context, "command");

        DynamicKeybindProperties properties = new DynamicKeybindProperties();
        properties.command = command;
        List<Integer> keys = new ArrayList<>();
        keys.add(key);
        DynamicKeybindHandler.addKeybind(name, keys, properties);

        return 0;
    }
}
