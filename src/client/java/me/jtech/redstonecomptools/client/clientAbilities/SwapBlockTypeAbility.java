package me.jtech.redstonecomptools.client.clientAbilities;

import me.jtech.redstonecomptools.client.utility.Toaster;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SwapBlockTypeAbility extends BaseAbility{
    private boolean concrete = true;

    public SwapBlockTypeAbility(String identifier) {
        super("Swap Block Type", false, GLFW.GLFW_KEY_J, false, false, Identifier.of(identifier));
    }

    @Override
    public void postInit() {

    }

    @Override
    public void toggled(boolean state) { //TODO comment this
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        PlayerInventory inventory = client.player.getInventory();
        ItemStack item = inventory.getMainHandStack();
        String type = item.getItem().toString();

        //TODO remove this sout
        System.out.println(type);

        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
            concrete = !concrete;
            Toaster.sendToast(client, Text.literal("Swap Block"), Text.literal("Swapped to " + (concrete?"concrete":"wool") + "mode"));
            return;
        }

        if (type.contains("stained_glass")) {
            if (concrete) {

            } else {

            }
        }

        if (type.contains("wool")) {

        }

        if (type.contains("concrete")) {

        }
    }

    @Override
    public void used() {

    }

    public void toGlass(String name, String remove, String add) {

    }
}
