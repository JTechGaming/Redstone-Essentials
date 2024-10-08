package me.jtech.redstonecomptools.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class KeybindWidget extends ButtonWidget {
    private final KeybindEntry keybind;
    private boolean expanded = false;
    private KeybindScreen parentScreen;

    public KeybindWidget(KeybindScreen parentScreen, KeybindEntry keybind, int x, int y, int width, int height) {
        super(x, y, width, height, Text.literal(keybind.getName()), button -> {}, null);
        this.keybind = keybind;
        this.parentScreen = parentScreen;
    }

    @Override
    public void onPress() {
        // Toggle expanded state on click
        expanded = !expanded;
        if (expanded) {
            // Render properties (toggle buttons, command, key, etc.)
            this.parentScreen.addDrawableChild(new PublicButtonWidget(this.getX() + 10, this.getY() + 25, 80, 20, Text.literal("Edit"), b -> {
                // Open KeybindEditorScreen for this keybind
                MinecraftClient.getInstance().setScreen(new KeybindEditorScreen(this.keybind));
            }, null));
            // Add additional UI elements for properties...
        } else {
            // Collapse the widget by removing extra buttons
            parentScreen.updateKeybinds();
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        // If expanded, render more details like properties
        if (expanded) {
            // Render keybind properties (e.g., key combination, command)
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Command: " + keybind.getCommand(), this.getX(), this.getY(), 0xFFFFFF);
            // Add boolean flags, key, etc.
        }
    }
}

