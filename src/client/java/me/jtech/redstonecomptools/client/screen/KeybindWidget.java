package me.jtech.redstonecomptools.client.screen;

import me.jtech.redstonecomptools.Redstonecomptools;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
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
            }).dimensions(this.getX() + 310, this.getY()+(this.height/2), 80, 20).tooltip(Tooltip.of(Text.literal("Edit this keybindings settings"))).build());

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

            int padding = 20;
            // -javaagent:"C:\Users\jaspe\.gradle\caches\modules-2\files-2.1\net.fabricmc\sponge-mixin\0.15.3+mixin.0.8.7\51ee0a44ab05f6fddd66b09e66b3a16904f9c55d\sponge-mixin-0.15.3+mixin.0.8.7.jar"
            context.fill(this.getX()+this.width, this.getY(), this.getX()+this.width+200, this.getY()+this.height*2, 0xAF221B1B); //0x221B1B
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Command: " + keybind.getCommand(), this.getX() + 220, this.getY()+this.height-5, 0xFFFFFF);

            // TODO Display the flags, key, etc.
        } else {
            // Toggle button visibility
            if (button != null) {
                button.visible = false;
            }
        }
    }
}

