package me.jtech.redstonecomptools.client.clientAbilities;

import me.jtech.redstonecomptools.client.utility.Toaster;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public abstract class BaseAbility {
    public String name;
    public static Identifier identifier;
    public boolean toggled;
    public KeyBinding keyBinding;
    private int key = GLFW.GLFW_KEY_P;

    private long actionTime = 0L;

    public void init() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redstonecomptools.%s".formatted(identifier),
                InputUtil.Type.KEYSYM,
                key,
                "category.redstonecomptools.abilities"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long currentTime = System.currentTimeMillis();
            if (keyBinding.isPressed() && currentTime - actionTime >= 200) {
                actionTime = currentTime;
                toggle();
            }
        });

        postInit();
    }

    public BaseAbility(String name, boolean toggled, int key) {
        this.name = name;
        this.toggled = toggled;
        this.key = key;
    }

    public void toggle() {
        toggled = !toggled;

        Toaster.sendToast(MinecraftClient.getInstance(), Text.literal(name), Text.literal("Was toggled " + (toggled? "On" : "Off")));

        toggled(toggled);
    }

    public void enable() {
        if (!toggled) {
            toggled(false);
            toggled = true;
        }
    }

    public void disable() {
        if (toggled) {
            toggled(true);
            toggled = false;
        }
    }

    public abstract void postInit();

    public abstract void toggled(boolean state);

    public abstract void used();
}
