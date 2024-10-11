package me.jtech.redstonecomptools.client.keybinds;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import me.jtech.redstonecomptools.client.screen.KeybindEditorScreen;
import me.jtech.redstonecomptools.client.screen.KeybindEntry;
import me.jtech.redstonecomptools.client.screen.KeybindRegistry;
import me.jtech.redstonecomptools.client.utility.Pair;
import me.jtech.redstonecomptools.networking.RunCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DynamicKeybindHandler {
    public static Map<String, Pair<List<Integer>, DynamicKeybindProperties>> keyBinds = new HashMap<>();
    private static boolean isWaitingForKey = false;

    private static final Gson GSON = new Gson();
    private static final Path CONFIG_FILE = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/dynamic_keybinds.json");

    public static void addKeybind(String name, List<Integer> key, DynamicKeybindProperties properties) {
        keyBinds.put(name, new Pair<>(key, properties));
    }

    public static void removeKeybind(String name) {
        keyBinds.remove(name);
    }

    public static void checkKeyPresses() {
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        for (Pair<List<Integer>, DynamicKeybindProperties> pair : keyBinds.values()) {
            int completionBuffer = 0;
            for (int i=0; i<pair.getFirst().size(); i++) {
                if (InputUtil.isKeyPressed(windowHandle, pair.getFirst().get(i))) {
                    completionBuffer++;
                    if (completionBuffer==pair.getFirst().size()) {
                        handleKeyPress(pair.getSecond());
                    }
                }
            }
        }
    }

    public static void handleKeyPress(DynamicKeybindProperties properties) {
        ClientPlayNetworking.send(new RunCommandPayload(properties.command));
    }

    public static void waitForKeyInput(KeybindEditorScreen handler) {
        isWaitingForKey = true;
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.currentScreen != null;
        List<Integer> keyCombo = new ArrayList<>();
        /**
         * Called right before a key press is handled.
         *
         * @param screen the screen in which the key was pressed
         * @param key the named key code which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
         * @param scancode the unique/platform-specific scan code of the keyboard input
         * @param modifiers a GLFW bitfield describing the modifier keys that are held down
         * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
         * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
         */
        ScreenKeyboardEvents.beforeKeyPress(client.currentScreen).register((screen, key, scancode, modifiers) -> {
            if (isWaitingForKey) {
                if (key == GLFW.GLFW_KEY_ENTER) {
                    isWaitingForKey = false;
                } else {
                    keyCombo.add(key);
                }
                handler.setKeys(keyCombo);
            }
        });
    }

    // Save keybinds to a config file
    public static void saveKeybinds() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(keyBinds, writer);
        } catch (IOException e) {
            e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }

    public static void setupScreenRegister() {
        for (String key : keyBinds.keySet()) {
            Pair<List<Integer>, DynamicKeybindProperties> value = keyBinds.get(key);
            KeybindEntry entry = new KeybindEntry(key, value.getSecond().command, value.getFirst(), false, false);
            KeybindRegistry.register(entry);
        }
    }
}
