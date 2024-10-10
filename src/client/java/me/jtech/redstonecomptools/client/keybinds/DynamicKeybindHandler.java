package me.jtech.redstonecomptools.client.keybinds;

import me.jtech.redstonecomptools.client.screen.KeybindEditorScreen;
import me.jtech.redstonecomptools.client.utility.Pair;
import me.jtech.redstonecomptools.networking.RunCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class DynamicKeybindHandler {
    public static Map<String, Pair<Integer, DynamicKeybindProperties>> keyBinds = new HashMap<>();
    private static boolean isWaitingForKey = false;

    public static void addKeybind(String name, int key, DynamicKeybindProperties properties) {
        keyBinds.put(name, new Pair<>(key, properties));
    }

    public static void checkKeyPresses() {
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        for (Pair<Integer, DynamicKeybindProperties> pair : keyBinds.values()) {
            if (InputUtil.isKeyPressed(windowHandle, pair.getFirst())) {
                handleKeyPress(pair.getSecond());
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
}
