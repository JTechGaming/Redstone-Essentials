package me.jtech.redstone_essentials.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.jtech.redstone_essentials.Redstone_Essentials;
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
import me.jtech.redstone_essentials.client.rendering.screen.rtbo.RTBOScreen;
import me.jtech.redstone_essentials.client.utility.ClientSelectionHelper;
import me.jtech.redstone_essentials.client.utility.ReadWriteSelection;
import me.jtech.redstone_essentials.client.utility.Toaster;
import me.jtech.redstone_essentials.networking.InfoPackets;
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
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
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
    public static final Logger LOGGER = LoggerFactory.getLogger(Redstone_Essentials.MOD_ID);

    private static KeyBinding openDynamicKeybindMenuKeybinding;
    private static KeyBinding openRTBOMenuKeybinding;

    private List<String> bitmapBuffer = new ArrayList<>();

    public static boolean packetsEnabled = true;

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
        openRTBOMenuKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redstone_essentials.open_rtbo_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
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
            if (openRTBOMenuKeybinding.isPressed()) {
                if (!(client.currentScreen instanceof RTBOScreen)) {
                    client.setScreen(new RTBOScreen(client.currentScreen));
                }
            }
        });

        WorldRenderEvents.LAST.register((context) -> {
            ClientSelectionHelper.renderAll();
            BlockOverlayRenderer.renderAll(context.matrixStack(), context.consumers());
        });

        LOGGER.info("Setting up Client-Side Packets...");
        ClientPlayNetworking.registerGlobalReceiver(ClientsRenderPingPayload.ID, (((payload, context) -> {
            context.client().execute(() -> {
                boolean isSelectionOverlay = payload.bools().x()==1;
                boolean isRTBOOverlay = payload.bools().y()==1;
                if ((!isSelectionOverlay && !Config.receive_pings) || (isSelectionOverlay && !Config.receive_selections) || (isRTBOOverlay && !Config.receive_rtbo)) {
                    return;
                }
                Color color = Color.getHSBColor(payload.rgb().x, payload.rgb().y, payload.rgb().z);
                if (!isSelectionOverlay) {
                    color = Color.decode(Config.multiplayer_ping_color);
                    for (int i=0; i<Config.player_colors1.size(); i++) {
                        String name = Config.player_colors1.get(i);
                        if (name.equalsIgnoreCase(payload.owningPlayer()) && Config.player_colors2.size()>=i) {
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
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
                        context.player().sendMessage(Text.literal( payload.flag1() + " cleared their pings!").withColor(0xFFA500));
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
}
