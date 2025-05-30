package me.jtech.redstone_essentials.client.clientAbilities.qolTools;

import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.networking.payloads.c2s.SetItemPayload;
import me.jtech.redstone_essentials.utility.Pair;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalStrengthGiver { //TODO comment this
    private static KeyBinding barrel;
    private static boolean barrelBtnHeld = false;

    private static KeyBinding shulker;
    private static boolean shulkerBtnHeld = false;

    public static void setupKeybinds() {
        barrel = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redstone_essentials.barrel",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.redstone_essentials.qol"
        ));

        shulker = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redstone_essentials.shulker",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.redstone_essentials.qol"
        ));
    }

    public static void processBarrel() {
        if (!barrel.isPressed()) {
            barrelBtnHeld = false;
        }
        if (barrelBtnHeld) {
            return;
        }
        if (barrel.isPressed()) {
            // Check if shift is pressed
            boolean shift = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT);

            Map<Integer, Pair<Integer, Integer>> keyData = setupKeycodes(shift);

            // Check for pressed keys
            for (Map.Entry<Integer, Pair<Integer, Integer>> entry : keyData.entrySet()) {
                if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), entry.getKey())) {
                    barrelBtnHeld = true;

                    int ss = entry.getValue().getFirst();
                    int axes = entry.getValue().getSecond();

                    // Process the barrel with the given ss and axes
                    ItemStack barrelItem = new ItemStack(Items.BARREL);
                    List<ItemStack> stacks = new ArrayList<>();
                    for (int i = axes; i > 0; i--) {
                        stacks.add(new ItemStack(Items.WOODEN_AXE));
                    }

                    // Set NBT and other properties for the barrel
                    barrelItem.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks));
                    barrelItem.set(DataComponentTypes.ITEM_NAME, Text.literal("Signal Strength " + ss));
                    barrelItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Signal Strength " + ss));

                    // Send item packet to the server
                    if (Redstone_Essentials_Client.packetsEnabled)
                        ClientPlayNetworking.send(new SetItemPayload(barrelItem, MinecraftClient.getInstance().player.getInventory().getEmptySlot()));

                    break;  // Exit loop since a key has been processed
                }
            }
        }
    }

    public static void processShulker() {
        if (!shulker.isPressed()) {
            shulkerBtnHeld = false;
        }
        if (shulkerBtnHeld) {
            return;
        }
        if (shulker.isPressed()) {
            // Check if shift is pressed
            boolean shift = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT);

            Map<Integer, Pair<Integer, Integer>> keyData = setupKeycodes(shift);

            // Check for pressed keys
            for (Map.Entry<Integer, Pair<Integer, Integer>> entry : keyData.entrySet()) {
                if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), entry.getKey())) {
                    shulkerBtnHeld = true;

                    int ss = entry.getValue().getFirst();
                    int axes = entry.getValue().getSecond();

                    // Process the shulker with the given ss and axes
                    ItemStack shulkerItem = new ItemStack(Items.WHITE_SHULKER_BOX);
                    List<ItemStack> stacks = new ArrayList<>();
                    for (int i = axes; i > 0; i--) {
                        stacks.add(new ItemStack(Items.WOODEN_AXE));
                    }

                    // Set NBT and other properties for the shulker
                    shulkerItem.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks));
                    shulkerItem.set(DataComponentTypes.ITEM_NAME, Text.literal("Signal Strength " + ss));
                    shulkerItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Signal Strength " + ss));

                    // Send item packet to the server
                    if (Redstone_Essentials_Client.packetsEnabled)
                        ClientPlayNetworking.send(new SetItemPayload(shulkerItem, MinecraftClient.getInstance().player.getInventory().getEmptySlot()));

                    break;  // Exit loop since a key has been processed
                }
            }
        }
    }

    private static Map<Integer, Pair<Integer, Integer>> setupKeycodes(boolean shift) {
        // Map of key codes to signal strength and axes values
        Map<Integer, Pair<Integer, Integer>> keyData = new HashMap<>();
        keyData.put(GLFW.GLFW_KEY_0, new Pair<>(10, 18));
        keyData.put(GLFW.GLFW_KEY_1, new Pair<>(shift ? 11 : 1, shift ? 20 : 1));
        keyData.put(GLFW.GLFW_KEY_2, new Pair<>(shift ? 12 : 2, shift ? 22 : 2));
        keyData.put(GLFW.GLFW_KEY_3, new Pair<>(shift ? 13 : 3, shift ? 24 : 4));
        keyData.put(GLFW.GLFW_KEY_4, new Pair<>(shift ? 14 : 4, shift ? 26 : 6));
        keyData.put(GLFW.GLFW_KEY_5, new Pair<>(shift ? 15 : 5, shift ? 27 : 8));
        keyData.put(GLFW.GLFW_KEY_6, new Pair<>(6, 10));
        keyData.put(GLFW.GLFW_KEY_7, new Pair<>(7, 12));
        keyData.put(GLFW.GLFW_KEY_8, new Pair<>(8, 14));
        keyData.put(GLFW.GLFW_KEY_9, new Pair<>(9, 16));

        return keyData;
    }
}
