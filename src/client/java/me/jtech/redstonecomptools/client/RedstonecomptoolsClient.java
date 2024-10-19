package me.jtech.redstonecomptools.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstonecomptools.client.axiomExtensions.ServiceHelper;
import me.jtech.redstonecomptools.client.axiomExtensions.tools.forceNeighborUpdatesTool;
import me.jtech.redstonecomptools.client.keybinds.DynamicKeybindHandler;
import me.jtech.redstonecomptools.client.keybinds.DynamicKeybindProperties;
import me.jtech.redstonecomptools.client.qolTools.SignalStrengthGiver;
import me.jtech.redstonecomptools.client.rendering.BlockOverlayRenderer;
import me.jtech.redstonecomptools.client.screen.KeybindScreen;
import me.jtech.redstonecomptools.networking.ClientsRenderPingPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class RedstonecomptoolsClient implements ClientModInitializer { //TODO comment this
    public static final Logger LOGGER = LoggerFactory.getLogger("redstonecomptools");
    public static final String MOD_ID = "redstonecomptools";

    private static KeyBinding openDynamicKeybindMenuKeybinding;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initialising Registers...");
        AbilityManager.init();
        Abilities abilities = Abilities.getInstance();
        AbilityManager.initAbilities();

        LOGGER.info("Setting up Keybindings...");
        DynamicKeybindHandler.loadKeybinds();

        openDynamicKeybindMenuKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redstonecomptools.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.redstonecomptools.main"
        ));

        LOGGER.info("Setting up QOL features...");
        // Setup keybinds for barrel and shulker giver
        SignalStrengthGiver.setupKeybinds();

        LOGGER.info("Registering axiom extension tools...");
        ServiceHelper.getToolRegistryService().register(new forceNeighborUpdatesTool());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SignalStrengthGiver.processBarrel();
            SignalStrengthGiver.processShulker();

            DynamicKeybindHandler.checkKeyPresses();

            if (openDynamicKeybindMenuKeybinding.isPressed()) {
                if (!(client.currentScreen instanceof KeybindScreen)) {
                    client.setScreen(new KeybindScreen(client.currentScreen));
                }
            }
        });

        registerCommand();

        WorldRenderEvents.LAST.register((context) -> {
            BlockOverlayRenderer.renderAll(context.matrixStack(), context.consumers());
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientsRenderPingPayload.ID, (((payload, context) -> {
            context.client().execute(() -> {
                Color color = Color.getHSBColor(payload.color().x, payload.color().y, payload.color().z);
                BlockOverlayRenderer.addOverlay(payload.blockPos(), color, new Vec3i((int) payload.size().x, (int) payload.size().y, (int) payload.size().z));
            });
        })));
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
