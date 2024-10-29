package me.jtech.redstone_essentials.client.rendering.screen.widgets;

import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindEditorScreen;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindEntry;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class KeybindWidget extends ButtonWidget { //TODO comment this
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
                MinecraftClient.getInstance().setScreen(new KeybindEditorScreen(this.keybind, this.keybind.getKey()));
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
//TODO comment this
            context.fill(this.getX()+this.width, this.getY(), this.getX()+this.width+200, this.getY()+this.height*2, 0xAF221B1B); //0x221B1B
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Command: " + keybind.getCommand(), this.getX() + 205, this.getY()+this.height-5, 0xFFFFFF);

            // TODO Display the flags, key, etc.
        } else {
            // Toggle button visibility
            if (button != null) {
                button.visible = false;
            }
        }
    }
}

