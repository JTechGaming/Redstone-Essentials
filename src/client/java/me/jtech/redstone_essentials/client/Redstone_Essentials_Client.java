package me.jtech.redstone_essentials.client;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.clientAbilities.SelectionAbility;
import me.jtech.redstone_essentials.client.debugger.ClientDebugSession;
import me.jtech.redstone_essentials.debugger.DebugManager;
import me.jtech.redstone_essentials.debugger.DebuggerSession;
import me.jtech.redstone_essentials.client.editor.imgui.ImGuiImplementation;
import me.jtech.redstone_essentials.client.rendering.gui.RealtimeByteOutputRenderer;
import me.jtech.redstone_essentials.client.keybinds.DynamicKeybindHandler;
import me.jtech.redstone_essentials.client.clientAbilities.qolTools.SignalStrengthGiver;
import me.jtech.redstone_essentials.client.rendering.BlockOverlayRenderer;
import me.jtech.redstone_essentials.client.rendering.screen.BitmapPrinterScreen;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindScreen;
import me.jtech.redstone_essentials.client.rendering.screen.rtbo.RTBOScreen;
import me.jtech.redstone_essentials.client.utility.ClientSelectionHelper;
import me.jtech.redstone_essentials.client.utility.ReadWriteSelection;
import me.jtech.redstone_essentials.client.utility.Toaster;
import me.jtech.redstone_essentials.networking.InfoPackets;
import me.jtech.redstone_essentials.networking.payloads.c2s.C2SDebuggerPacket;
import me.jtech.redstone_essentials.networking.payloads.c2s.C2SInfoPacket;
import me.jtech.redstone_essentials.networking.payloads.s2c.ServerSendBitmapPayload;
import me.jtech.redstone_essentials.client.utility.ServerAccessibleScreens;
import me.jtech.redstone_essentials.IO.Config;
import me.jtech.redstone_essentials.networking.payloads.c2s.SetBlockPayload;
import me.jtech.redstone_essentials.networking.payloads.s2c.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Redstone_Essentials_Client implements ClientModInitializer { //TODO comment this
    public static final Logger LOGGER = LoggerFactory.getLogger(Redstone_Essentials.MOD_ID);

    private static KeyBinding openDynamicKeybindMenuKeybinding;
    private static KeyBinding openRTBOMenuKeybinding;
    private static KeyBinding openEditorKeybinding;

    private List<String> bitmapBuffer = new ArrayList<>();

    public static boolean packetsEnabled = true;
    public static boolean shouldRender = false;

    public ClientDebugSession debugSession = new ClientDebugSession();

    private static Redstone_Essentials_Client instance;

    @Override
    public void onInitializeClient() {
        instance = this;
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
        openRTBOMenuKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redstone_essentials.open_rtbo_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.redstone_essentials.main"
        ));
        openEditorKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redstone_essentials.open_editor", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_F10, // The keycode of the key
                "category.redstone_essentials.main" // The translation key of the keybinding's category.
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

        LOGGER.info("Registering Events");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openEditorKeybinding.wasPressed()) {
                toggleVisibility();
            }
            SignalStrengthGiver.processBarrel();
            SignalStrengthGiver.processShulker();

            DynamicKeybindHandler.checkKeyPresses();

            if (openDynamicKeybindMenuKeybinding.isPressed()) {
                if (!(client.currentScreen instanceof KeybindScreen)) {
                    client.setScreen(new KeybindScreen(client.currentScreen));
                }
            }
            if (openRTBOMenuKeybinding.isPressed()) {
                if (!(client.currentScreen instanceof RTBOScreen)) {
                    client.setScreen(new RTBOScreen(client.currentScreen));
                }
            }
        });

        // Prevent Minecraft from locking the cursor when clicking
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (shouldRender) {
                if (openEditorKeybinding.wasPressed()) {
                    toggleVisibility();
                }
                if (!ImGuiImplementation.grabbed) {
                    KeyBinding.unpressAll();
                    unlockCursor();
                }

                handleKeypresses();
            }
        });

        WorldRenderEvents.LAST.register((context) -> {
            ClientSelectionHelper.renderAll();
            BlockOverlayRenderer.renderAll(context.matrixStack(), context.consumers());

//            long currentTime = System.currentTimeMillis();
//            DebuggerSession session = DebugManager.debugSessions;
//            if (session == null) {
//                return;
//            }
//            session.highlightedBlocks.entrySet().removeIf(entry -> currentTime - entry.getValue() > 3000); // Remove highlights older than 3 seconds
//
//            for (BlockPos pos : session.highlightedBlocks.keySet()) {
//                renderHighlight(context.matrixStack(), pos, currentTime);
//            }
        });

        LOGGER.info("Setting up Client-Side Packets...");
        ClientPlayNetworking.registerGlobalReceiver(ClientsRenderPingPayload.ID, (((payload, context) -> {
            context.client().execute(() -> {
                boolean isSelectionOverlay = payload.bools().x() == 1;
                boolean isRTBOOverlay = payload.bools().y() == 1;
                if ((!isSelectionOverlay && !Config.receive_pings) || (isSelectionOverlay && !Config.receive_selections) || (isRTBOOverlay && !Config.receive_rtbo)) {
                    return;
                }
                Color color = Color.getHSBColor(payload.rgb().x, payload.rgb().y, payload.rgb().z);
                if (!isSelectionOverlay) {
                    color = Color.decode(Config.multiplayer_ping_color);
                    for (int i = 0; i < Config.player_colors1.size(); i++) {
                        String name = Config.player_colors1.get(i);
                        if (name.equalsIgnoreCase(payload.owningPlayer()) && Config.player_colors2.size() >= i) {
                            color = Color.decode(Config.player_colors2.get(i));
                            break;
                        }
                    }
                }
                Vec3i size = new Vec3i((int) payload.size().x, (int) payload.size().y, (int) payload.size().z);
                new BlockOverlayRenderer(payload.blockPos(), color, size, true, isRTBOOverlay, SelectionAbility.selectionContext, payload.label(), payload.owningPlayer()).addOverlay(payload.blockPos(), color, size, isSelectionOverlay);
                if (isRTBOOverlay) {
                    RealtimeByteOutputRenderer.realtimeByteOutputList.add(new SelectionData(payload.blockPos(), Color.getHSBColor(payload.rgb().x, payload.rgb().y, payload.rgb().z), size, context.player().getName().getString() + " : " + payload.label(), true, payload.owningPlayer()));
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
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
        })));

        ClientPlayNetworking.registerGlobalReceiver(FinishBitmapPrintPayload.ID, (((payload, context) -> {
            context.client().execute(BitmapPrinterScreen::finishPrint);
        })));

        ClientPlayNetworking.registerGlobalReceiver(ClientSetBlockPayload.ID, (((payload, context) -> {
            if (Redstone_Essentials_Client.packetsEnabled)
                context.client().execute(() -> ClientPlayNetworking.send(new SetBlockPayload(payload.blockPos(), payload.blockName(), payload.supportBlockName())));
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

        ClientPlayNetworking.registerGlobalReceiver(S2CDebuggerPacket.ID, (((payload, context) -> {
            context.client().execute(() -> {
                S2CDebuggerPacket.S2CInfoType infoID = S2CDebuggerPacket.S2CInfoType.fromId(payload.type());
                switch (infoID) {
                    case TEST -> {
                        context.player().sendMessage(Text.literal("Test"));
                    }
                    case null -> {}
                    default -> throw new IllegalStateException("Unexpected value: " + payload.type());
                }
            });
        })));

        ClientPlayNetworking.registerGlobalReceiver(S2CInfoPacket.ID, ((((payload, context) -> {
            context.client().execute(() -> {
                InfoPackets.S2C infoID = InfoPackets.getS2CEnum(payload.infoID());
                switch (infoID) {
                    case CLIENT_RECEIVE_SERVER_VERSION -> {
                        String serverVersion = payload.flag1();
                        String clientVersion = Redstone_Essentials.MOD_VERSION;
                        if (!clientVersion.equalsIgnoreCase(serverVersion)) {
                            Toaster.sendToast(MinecraftClient.getInstance(), Text.literal("Mod version mismatch"), Text.literal("The server and client have a different version of redstone essentials. Expect some stuff to be broken."), 1500L);
                            //disableFeatures(clientVersion, serverVersion);
                        }
                    }
                    case RW_BIN -> {
                        ReadWriteSelection.mode = payload.flag1();
                        ReadWriteSelection.startSelection();
                    }
                    case CLEAR_PINGS -> {
                        BlockOverlayRenderer.clearOverlays(payload.flag1());
                        if (!Config.announce_clear) {
                            return;
                        }
                        context.player().sendMessage(Text.literal(payload.flag1() + " cleared their pings!").withColor(0xFFA500));
                    }
                    case SEND_PINGS_TO_NEW_CLIENT -> {
                        List<SelectionData> selections = new ArrayList<>();
                        for (BlockOverlayRenderer renderer : BlockOverlayRenderer.getOverlays()) {
                            if (renderer.isMultiplayerPing || renderer.isRTBO) {
                                continue;
                            }
                            selections.add(new SelectionData(renderer.blockPos, renderer.color, renderer.size, renderer.getLabel(), false, renderer.owningPlayer));
                        }
                        ClientPlayNetworking.send(new C2SInfoPacket(InfoPackets.getInt(InfoPackets.C2S.SEND_PINGS_TO_NEW_CLIENT), payload.flag1(), "", "", selections));
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + payload.infoID());
                }
            });
        }))));

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            if (Redstone_Essentials_Client.packetsEnabled)
                ClientPlayNetworking.send(new C2SInfoPacket(InfoPackets.getInt(InfoPackets.C2S.ASK_SERVER_MOD_VERSION), Redstone_Essentials.MOD_VERSION, "", "", new ArrayList<>()));
            BlockOverlayRenderer.loadSessions();
        }));

        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            BlockOverlayRenderer.saveSessions();
            BlockOverlayRenderer.clearAllOverlays();
        }));
    }

    public static void disableFeatures(String clientVersion, String serverVersion) {
        // in future versions, figure out exactly what needs to be disabled
        Redstone_Essentials_Client.packetsEnabled = false;
    }

    boolean saveKeyPressed = false;
    boolean closeKeyPressed = false;
    boolean reloadKeyPressed = false;

    private void handleKeypresses() {
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        boolean ctrlPressed = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_RIGHT_CONTROL);
        boolean shiftPressed = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

        if (ctrlPressed) {
//            if (currentPack == null || EditorWindow.openFiles.isEmpty() || EditorWindow.currentFile == null) {
//                return;
//            }
            if (InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_S)) {
                if (saveKeyPressed) {
                    return;
                }
                saveKeyPressed = true;
//                if (shiftPressed) {
//                    // Handle Ctrl+Shift+S
//                    FileUtils.saveAllFiles();
//                } else {
//                    // Handle Ctrl+S
//                    FileUtils.saveSingleFile(EditorWindow.currentFile.getIdentifier(), EditorWindow.currentFile.getExtension().getExtension(), FileUtils.getContent(EditorWindow.currentFile));
//                }
            } else {
                saveKeyPressed = false;
            }
        }
    }

    private void toggleVisibility() {
        shouldRender = !shouldRender;

        if (shouldRender) {
            ImGuiImplementation.aspectRatio = (float) MinecraftClient.getInstance().getWindow().getWidth() / MinecraftClient.getInstance().getWindow().getHeight();
            unlockCursor();
        } else {
            lockCursor();
        }
        // Uncomment if the game mode switching is needed
        GameMode gameMode = shouldRender ? GameMode.SPECTATOR : getPreviousGameMode();
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        if (client.player.hasPermissionLevel(2)) {
            if (gameMode.equals(GameMode.CREATIVE)) {
                client.player.networkHandler.sendCommand("gamemode creative");
            } else if (gameMode.equals(GameMode.SURVIVAL)) {
                client.player.networkHandler.sendCommand("gamemode survival");
            } else if (gameMode.equals(GameMode.SPECTATOR)) {
                client.player.networkHandler.sendCommand("gamemode spectator");
            } else if (gameMode.equals(GameMode.ADVENTURE)) {
                client.player.networkHandler.sendCommand("gamemode adventure");
            } else {
                LOGGER.error("Unknown game mode: {}", gameMode);
            }
        }

        ImGuiImplementation.shouldRender = shouldRender;
    }

    private static void unlockCursor() {
        MinecraftClient client = MinecraftClient.getInstance();
        GLFW.glfwSetInputMode(client.getWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        client.mouse.unlockCursor();
    }

    private GameMode getPreviousGameMode() {
        ClientPlayerInteractionManager clientPlayerInteractionManager = MinecraftClient.getInstance().interactionManager;
        GameMode gameMode = clientPlayerInteractionManager.getPreviousGameMode();
        if (gameMode != null) {
            return gameMode;
        } else {
            return clientPlayerInteractionManager.getCurrentGameMode() == GameMode.CREATIVE ? GameMode.SURVIVAL : GameMode.CREATIVE;
        }
    }

    private static void lockCursor() {
        MinecraftClient client = MinecraftClient.getInstance();
        GLFW.glfwSetInputMode(client.getWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        client.mouse.lockCursor();
    }

    private void renderHighlight(MatrixStack matrices, BlockPos pos, long time) {
        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate buffer = client.getBufferBuilders().getEntityVertexConsumers();

        float alpha = 0.5f + 0.5f * (float) Math.sin((time % 1000) / 1000.0 * Math.PI * 2); // Pulsing effect

        WorldRenderer.drawBox(matrices, buffer.getBuffer(RenderLayer.getLines()), pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, 1.0f, 0.0f, 0.0f, alpha); // Red outline
    }

    public static Redstone_Essentials_Client getInstance() {
        return instance;
    }
}
