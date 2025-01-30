package me.jtech.redstone_essentials.client.keybinds;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindEditorScreen;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindEntry;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindRegistry;
import me.jtech.redstone_essentials.client.utility.Toaster;
import me.jtech.redstone_essentials.utility.Pair;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.logging.log4j.core.jmx.Server;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class DynamicKeybindHandler { //TODO comment this
    public static Map<String, Pair<List<Integer>, DynamicKeybindProperties>> keyBinds = new HashMap<>();

    private static final Gson GSON = new Gson();
    private static final Path CONFIG_FILE = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/redstone_essentials/dynamic_keybinds.json");
    private static boolean isInitialised = false;
    private static boolean hasProcessedKey = false;

    public static boolean isReceiving = false;

    private static final List<Integer> pressedKeys = new ArrayList<>();
    private static KeybindEditorScreen parentScreen;

    public static void addKeybind(String name, List<Integer> key, DynamicKeybindProperties properties) {
        keyBinds.put(name, new Pair<>(key, properties));
    }

    public static void removeKeybind(String name) {
        keyBinds.remove(name);
    }

    public static void setupKeyDetection(KeybindEditorScreen scr) {
        DynamicKeybindHandler.parentScreen = scr;
        if (!isInitialised) {
            ScreenKeyboardEvents.beforeKeyPress(parentScreen).register((screen, keyCode, scanCode, modifiers) -> {
                if (isReceiving) {
                    if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                        if (!pressedKeys.contains(GLFW.GLFW_KEY_LEFT_SHIFT) && !pressedKeys.contains(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                            pressedKeys.add(GLFW.GLFW_KEY_LEFT_SHIFT);
                        }
                    } else if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
                        if (!pressedKeys.contains(GLFW.GLFW_KEY_LEFT_CONTROL) && !pressedKeys.contains(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                            pressedKeys.add(GLFW.GLFW_KEY_LEFT_CONTROL);
                        }
                    } else {
                        if (!pressedKeys.contains(keyCode)) {
                            pressedKeys.add(keyCode);
                        }
                    }
                    parentScreen.setKeys(new ArrayList<>(pressedKeys));
                }
            });

            ScreenKeyboardEvents.beforeKeyRelease(parentScreen).register((screen, keyCode, scanCode, modifiers) -> {
                if (isReceiving) {
                    for (Integer pressedKey : pressedKeys) {
                        System.out.println(pressedKey);
                    }
                    endKeyDetection(parentScreen);
                }
            });

            isInitialised = true;
        }
    }

    public static void endKeyDetection(KeybindEditorScreen parentScreen) {
        if (!isReceiving) return;
        isReceiving = false;
        parentScreen.setKeys(new ArrayList<>(pressedKeys));
        pressedKeys.clear();
        parentScreen.resetInputKey();
    }

    public static void checkKeyPresses() {
        boolean shouldProcessKey = checkShouldUpdate();
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        for (Pair<List<Integer>, DynamicKeybindProperties> pair : keyBinds.values()) {
            int completionBuffer = 0;
            for (int i=0; i<pair.getFirst().size(); i++) {
                if (InputUtil.isKeyPressed(windowHandle, pair.getFirst().get(i))) {
                    completionBuffer++;
                    if (completionBuffer==pair.getFirst().size()) {
                        DynamicKeybindProperties properties = pair.getSecond();
                        if (!hasProcessedKey && shouldProcessKey) {
                            hasProcessedKey = !properties.hasHoldKey; // Hold key implementation
                            if (properties.hasSendToast) {
                                Toaster.sendToast(MinecraftClient.getInstance(), Text.literal(properties.name), Text.literal(properties.toastMessage));
                            }

                            if (properties.hasToggleInterval) {
                                properties.setIntervalToggled(!properties.isIntervalToggled());
                            }

                            handleKeyPress(properties); // Handle the keypress
                        }
                    }
                } else {
                    hasProcessedKey = false;
                }
            }

            if (pair.getSecond().hasToggleInterval && pair.getSecond().isIntervalToggled()) {
                DynamicKeybindProperties properties = pair.getSecond();
                properties.setCurrentInterval(properties.getCurrentInterval() + 1);
                if (properties.getCurrentInterval() >= properties.interval) {
                    properties.setCurrentInterval(0);
                    handleKeyPress(properties);
                }
            }
        }
    }

    public static void handleKeyPress(DynamicKeybindProperties properties) {
        String command = properties.command;
        if (properties.hasCycleState && !properties.cycleStates.isEmpty()) {
            properties.setCurrentCycleState((properties.getCurrentCycleState() + 1) % properties.cycleStates.size());
            if (properties.cycleStates.get(properties.getCurrentCycleState()).isBlank()) properties.setCurrentCycleState((properties.getCurrentCycleState() + 1) % properties.cycleStates.size());
            command = properties.cycleStates.get(properties.getCurrentCycleState());
        }
        if (properties.copyText) {
            StringSelection selection = new StringSelection(properties.copyTextMessage);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatCommand(command);
        }
    }

    // Save keybinds to a config file
    public static void saveKeybinds() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(keyBinds, writer);
        } catch (IOException e) {
            Redstone_Essentials_Client.LOGGER.error(String.valueOf(e));
        }
    }

    // Load keybinds from the config file
    public static void loadKeybinds() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                Type type = new TypeToken<Map<String, Pair<List<Integer>, DynamicKeybindProperties>>>() {}.getType();
                keyBinds = GSON.fromJson(reader, type);
                setupScreenRegister();
            } catch (IOException e) {
                Redstone_Essentials_Client.LOGGER.error(String.valueOf(e));
            }
        }
    }

    public static void setupScreenRegister() {
        for (String key : keyBinds.keySet()) {
            Pair<List<Integer>, DynamicKeybindProperties> value = keyBinds.get(key);
            KeybindEntry entry = new KeybindEntry(key, value.getSecond().command, value.getFirst(), false, false);
            KeybindRegistry.register(entry, value.getSecond());
        }
    }

    public static boolean checkShouldUpdate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null)
            return false;
        return !client.inGameHud.getChatHud().isChatFocused();
    }

    public static DynamicKeybindProperties getKeybindProperties(String keybindName) {
        Pair<List<Integer>, DynamicKeybindProperties> keybind = keyBinds.get(keybindName);
        return keybind != null ? keybind.getSecond() : null;
    }
}
