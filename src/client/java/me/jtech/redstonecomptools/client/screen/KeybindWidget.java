package me.jtech.redstonecomptools.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class KeybindWidget extends ButtonWidget {
    private final KeybindEntry keybind;
    private boolean expanded = false;
    private KeybindScreen parentScreen;

    private ButtonWidget button;

    // Constructor using the default narration supplier
    public KeybindWidget(KeybindScreen parentScreen, KeybindEntry keybind, int x, int y, int width, int height) {
        super(x, y, width, height, Text.literal(keybind.getName()), button -> {}, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.keybind = keybind;
        this.parentScreen = parentScreen;
    }

    @Override
    public void onPress() {
        // Toggle expanded state on click
        expanded = !expanded;
        if (expanded) {
            // Render the button for editing the keybind
            button = this.parentScreen.addDrawableChild(ButtonWidget.builder(Text.literal("Edit"), b -> {
                // Open KeybindEditorScreen for this keybind
                MinecraftClient.getInstance().setScreen(new KeybindEditorScreen(this.keybind));
            }).dimensions(this.getX() + 200, this.getY() + 25, 80, 20).build());

            // TODO Add additional UI elements for properties
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
            // Render keybind properties as a tooltip (sort of)

            // Toggle button visibility
            if (button != null) {
                button.visible = true;
            }

            context.fill(this.getX(), this.getY(), this.getX() + this.width + 20, this.getY() + this.height + 20, 0x575252D); //0x575252
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Command: " + keybind.getCommand(), this.getX(), this.getY(), 0xFFFFFF);

            // TODO Display the flags, key, etc.
        } else {
            // Toggle button visibility
            if (button != null) {
                button.visible = false;
            }
        }
    }
}

