package me.jtech.redstonecomptools.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class KeybindScreen extends Screen {
    public static Screen parent;

    private List<KeybindEntry> keybindEntries; // Holds keybind data
    private ButtonWidget addButton;

    public KeybindScreen(Screen parent) {
        super(Text.literal("Dynamic Keybind Editor"));
        KeybindScreen.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Adding existing keybinds to the UI list
        int y = 20;
        for (KeybindEntry keybind : keybindEntries) {
            // Render each keybind entry
            this.addDrawableChild(new KeybindWidget(this, keybind, 10, y, 200, 20));
            y += 30;
        }

        // Add button to create new keybind
        addButton = new PublicButtonWidget(this.width / 2 - 50, this.height - 30, 100, 20, Text.literal("Add Keybind"), button -> {
            MinecraftClient.getInstance().setScreen(new KeybindEditorScreen(null)); // null indicates creating a new keybind
        }, null);
        this.addDrawableChild(addButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    public void updateKeybinds() {
        init(); // Reinitialize the screen to reflect changes
    }

    @Override
    protected <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        return super.addDrawableChild(drawableElement);
    }
}