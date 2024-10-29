package me.jtech.redstone_essentials.client.clientAbilities;

import me.jtech.redstone_essentials.client.utility.Toaster;
import me.jtech.redstone_essentials.networking.payloads.c2s.SetItemPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SwapBlockTypeAbility extends BaseAbility{ //TODO comment this
    private boolean concrete = true;

    public SwapBlockTypeAbility(String identifier) {
        super("Swap Block Type", false, GLFW.GLFW_KEY_J, false, false, Identifier.of("redstone_essentials", identifier));
    }

    @Override
    public void postInit() {

    }

    @Override
    public void toggled(boolean state) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        PlayerInventory inventory = client.player.getInventory();
        ItemStack item = inventory.getMainHandStack();

        if (item.equals(ItemStack.EMPTY)) return;

        String type = item.getItem().toString();

        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
            concrete = !concrete;
            Toaster.sendToast(client, Text.literal("Swap Block"), Text.literal("Swapped to " + (concrete?"concrete":"wool") + " mode"));
            return;
        }

        if (type.contains("stained_glass")) {
            type = type.replace("stained_glass", concrete?"concrete":"wool");
        } else if (type.contains("wool")) {
            type = type.replace("wool", "stained_glass");
        } else if (type.contains("concrete")) {
            type = type.replace("concrete", "stained_glass");
        }

        type = type.replaceAll("minecraft:", "");

        ItemStack returnItem = Registries.ITEM.get(Identifier.ofVanilla(type)).getDefaultStack();
        returnItem.set(DataComponentTypes.CUSTOM_NAME, item.get(DataComponentTypes.CUSTOM_NAME));
        returnItem.set(DataComponentTypes.ITEM_NAME, item.get(DataComponentTypes.ITEM_NAME));
        returnItem.setCount(item.getCount());

        ClientPlayNetworking.send(new SetItemPayload(returnItem, client.player.getInventory().selectedSlot));
    }

    @Override
    public void used() {

    }

    public void toGlass(String name, String remove, String add) {

    }
}
