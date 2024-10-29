package me.jtech.redstone_essentials.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.axiom.ServiceHelper;
import me.jtech.redstone_essentials.client.axiom.tools.forceNeighborUpdatesTool;
import me.jtech.redstone_essentials.client.clientAbilities.SelectionAbility;
import me.jtech.redstone_essentials.client.rendering.gui.RealtimeByteOutputRenderer;
import me.jtech.redstone_essentials.client.keybinds.DynamicKeybindHandler;
import me.jtech.redstone_essentials.client.keybinds.DynamicKeybindProperties;
import me.jtech.redstone_essentials.client.clientAbilities.qolTools.SignalStrengthGiver;
import me.jtech.redstone_essentials.client.rendering.BlockOverlayRenderer;
import me.jtech.redstone_essentials.client.rendering.screen.BitmapPrinterScreen;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindScreen;
import me.jtech.redstone_essentials.client.utility.ClientSelectionHelper;
import me.jtech.redstone_essentials.networking.payloads.c2s.ServerSendBitmapPayload;
import me.jtech.redstone_essentials.client.utility.ServerAccessibleScreens;
import me.jtech.redstone_essentials.IO.Config;
import me.jtech.redstone_essentials.networking.payloads.c2s.SetBlockPayload;
import me.jtech.redstone_essentials.networking.payloads.s2c.ClientSetBlockPayload;
import me.jtech.redstone_essentials.networking.payloads.s2c.ClientsRenderPingPayload;
import me.jtech.redstone_essentials.networking.payloads.s2c.FinishBitmapPrintPayload;
import me.jtech.redstone_essentials.networking.payloads.s2c.OpenScreenPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class Redstone_Essentials_Client implements ClientModInitializer { //TODO comment this
    public static final Logger LOGGER = LoggerFactory.getLogger("redstone_essentials");
    public static final String MOD_ID = "redstone_essentials";

    private static KeyBinding openDynamicKeybindMenuKeybinding;

    private List<String> bitmapBuffer = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        try {
            Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("redstone_essentials/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        openDynamicKeybindMenuKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redstone_essentials.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.redstone_essentials.main"
        ));

        LOGGER.info("Initialising Registers...");
        AbilityManager.init();
        Abilities abilities = Abilities.getInstance();
        AbilityManager.initAbilities();

        LOGGER.info("Setting up Keybindings...");
        DynamicKeybindHandler.loadKeybinds();

        LOGGER.info("Setting up QOL features...");
        // Setup keybinds for barrel and shulker giver
        SignalStrengthGiver.setupKeybinds();

        LOGGER.info("Registering axiom extension tools...");
        ServiceHelper.getToolRegistryService().register(new forceNeighborUpdatesTool());

        LOGGER.info("Registering Events");
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
            ClientSelectionHelper.renderAll();
            BlockOverlayRenderer.renderAll(context.matrixStack(), context.consumers());
        });

        LOGGER.info("Setting up Client-Side Packets...");
        ClientPlayNetworking.registerGlobalReceiver(ClientsRenderPingPayload.ID, (((payload, context) -> {
            context.client().execute(() -> {
                if ((!payload.isSelectionOverlay() && !Config.receive_pings) || (payload.isSelectionOverlay() && !Config.receive_selections) || (payload.isRTBOOverlay() && !Config.receive_rtbo)) {
                    return;
                }

                Color color = Color.getHSBColor(payload.rgb().x, payload.rgb().y, payload.rgb().z);
                if (!payload.isSelectionOverlay()) {
                    color = Color.decode(Config.multiplayer_ping_color);
                }

                Vec3i size = new Vec3i((int) payload.size().x, (int) payload.size().y, (int) payload.size().z);
                new BlockOverlayRenderer(payload.blockPos(), color, size, true, payload.isRTBOOverlay(), SelectionAbility.selectionContext, payload.label()).addOverlay(payload.blockPos(), color, size, payload.isSelectionOverlay());

                if (payload.isRTBOOverlay()) {
                    RealtimeByteOutputRenderer.realtimeByteOutputList.add(new SelectionData(payload.blockPos(), Color.getHSBColor(payload.rgb().x, payload.rgb().y, payload.rgb().z), size, context.player().getName().getString() + " : " + payload.label(), true));
                }
            });
        })));

        ClientPlayNetworking.registerGlobalReceiver(OpenScreenPayload.ID, (((payload, context) -> {
            context.client().execute(() -> {
                Screen targetScreen = ServerAccessibleScreens.screenList.get(payload.id());
                if (targetScreen == null) {
                    return;
                }
                try {
                    context.client().setScreen(targetScreen.getClass().getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
        })));

        ClientPlayNetworking.registerGlobalReceiver(FinishBitmapPrintPayload.ID, (((payload, context) -> {
            context.client().execute(BitmapPrinterScreen::finishPrint);
        })));

        ClientPlayNetworking.registerGlobalReceiver(ClientSetBlockPayload.ID, (((payload, context) -> {
            context.client().execute(() -> ClientPlayNetworking.send(new SetBlockPayload(payload.blockPos(), payload.blockName())));
        })));

        ClientPlayNetworking.registerGlobalReceiver(ServerSendBitmapPayload.ID, (((payload, context) -> {
            context.client().execute(() -> {
                bitmapBuffer.add(payload.bitmap());
                if (payload.finalBitmap()) {
                    BitmapPrinterScreen.bitmaps.addAll(bitmapBuffer);
                    bitmapBuffer.clear();
                }
            });
        })));

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            BlockOverlayRenderer.loadSessions();
        }));

        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            BlockOverlayRenderer.saveSessions();
            BlockOverlayRenderer.clearAllOverlays();
        }));
    }

    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("keybind")
                    .then(CommandManager.argument("name", StringArgumentType.string())
                            .then(CommandManager.argument("key", IntegerArgumentType.integer())
                                    .then(CommandManager.argument("command", greedyString())
                                            .executes(Redstone_Essentials_Client::executeCommand)))));
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
