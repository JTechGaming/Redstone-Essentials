package me.jtech.redstonecomptools.client.clientAbilities;

import me.jtech.redstonecomptools.client.utility.Toaster;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public abstract class BaseAbility {
    public String name; // The name of the ability
    public Identifier identifier; // The identifier of this ability
    public boolean toggled; // The toggle state of the ability
    public KeyBinding keyBinding; // The keybind. This is just a backend variable that doesnt need to be touched anywhere else
    private int key = GLFW.GLFW_KEY_P; // The key used to toggle the ability (default is P)
    public boolean shouldToast;
    public boolean canHold;

    private boolean hasProcessed = false; // Variable for spam prevention. Not used for anything else

    public void init() {
        // Setting up the keybind
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.%s".formatted(identifier),
                InputUtil.Type.KEYSYM,
                key,
                "category.redstonecomptools.abilities"
        ));

        // The keybind press detection
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.isPressed() && (!hasProcessed || canHold)) {
                hasProcessed = true; // Prevent spamming the keybind (unless canHold is enabled)
                toggle();
            }

            if (!keyBinding.isPressed()) {
                hasProcessed = false; // Reset spam detection when letting go of the key
            }
        });

        postInit(); // This function is called after the init function is done processing. It can be overridden in an ability class to execute some custom code
    }

    // Just a constructor to allow classes that extend this to provide custom values for name, toggled, and key.
    public BaseAbility(String name, boolean toggled, int key, boolean shouldToast, boolean canHold, Identifier identifier) {
        this.name = name;
        this.toggled = toggled;
        this.key = key;
        this.shouldToast = shouldToast;
        this.canHold = canHold;
        this.identifier = identifier;
    }

    // This function is called whenever the keybind is pressed and will toggle the active state
    public void toggle() {
        toggled = !toggled; // toggle the active state
        // Sends a toast to notify the player about the toggle (if shouldToast is enabled)
        // This function is called after the toggle function is done processing. It can be overridden in an ability class to execute some custom code
        toggled(toggled);

        if (shouldToast)
            Toaster.sendToast(MinecraftClient.getInstance(), Text.literal(name), Text.literal("Was toggled " + (toggled? "On" : "Off")));
    }

    // This just force enables the ability and is not implemented by default
    public void enable() {
        if (!toggled) { // Only actually does something if it is not already enabled
            toggled(false); // It also calls the toggled function to notify the class of the ability of the state change
            toggled = true; // Update toggle state
        }
    }

    // This just force enables the ability and is not implemented by default
    public void disable() {
        if (toggled) { // Only actually does something if it is not already disabled
            toggled(true); // It also calls the toggled function to notify the class of the ability of the state change
            toggled = false; // Update toggle state
        }
    }

    public abstract void postInit(); // The post init function. Overridable

    public abstract void toggled(boolean state); // The toggled function. Overridable

    public abstract void used(); // This function is not used, nor is it ever called, but it can be useful to create some custom behaviour
}
