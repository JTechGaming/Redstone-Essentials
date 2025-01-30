package me.jtech.redstone_essentials.client.rendering.screen.keybinds;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.client.rendering.screen.widgets.BitmapPrintListWidget;
import me.jtech.redstone_essentials.client.rendering.screen.widgets.DynamicKeybindListWidget;
import me.jtech.redstone_essentials.client.rendering.screen.widgets.KeybindWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class KeybindScreen extends Screen {
    private static final Logger log = LoggerFactory.getLogger(KeybindScreen.class);
    public static Screen parent;

    public static List<KeybindEntry> keybindEntries = KeybindRegistry.getKeybinds(); // Holds keybind data
    private ButtonWidget addButton;
    private DynamicKeybindListWidget scrollWidget;

    public KeybindScreen(Screen parent) {
        super(Text.literal("Dynamic Keybinding Editor"));
        Redstone_Essentials.shouldApplyButtonStyle = true;
        KeybindScreen.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        addDrawableChild(new TextWidget(this.width/2-100, -15, 200, 50, Text.literal("Dynamic Keybinds"), client.textRenderer));

        this.scrollWidget = new DynamicKeybindListWidget(client, this.width, 300, 20, 80);

        // Build the button to create a new keybind
        addButton = ButtonWidget.builder(Text.literal("Add Keybinding"), button -> {
            MinecraftClient.getInstance().setScreen(new KeybindEditorScreen(null, new ArrayList<>())); // null (and new arraylist) indicate creating a new keybinding
        }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build();
        // Add the button to the screen
        this.addDrawableChild(addButton);
        this.addDrawableChild(scrollWidget);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        Redstone_Essentials.shouldApplyButtonStyle = false;
        this.client.setScreen(null);
    }

    public void updateKeybinds() {
        init(); // Reinitialize the screen to reflect changes
    }

    @Override
    public <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        return super.addDrawableChild(drawableElement);
    }
}
