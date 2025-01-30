package me.jtech.redstone_essentials.client.rendering.screen.widgets;

import com.google.common.collect.ImmutableList;
import me.jtech.redstone_essentials.client.rendering.screen.BitmapPrinterScreen;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindEditorScreen;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindEntry;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public class DynamicKeybindListWidget extends ElementListWidget<DynamicKeybindListWidget.Entry> {
    public DynamicKeybindListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        List<KeybindEntry> keybinds = KeybindScreen.keybindEntries;
        for (KeybindEntry keybind : keybinds) {
            this.addEntry(new SelectionEntry(keybind));
        }
    }

    public int getRowWidth() {
        return 300;
    }

    @Environment(EnvType.CLIENT)
    public class SelectionEntry extends DynamicKeybindListWidget.Entry {
        private final ButtonWidget editButton;
        private final KeybindEntry keybind;

        SelectionEntry(final KeybindEntry keybind) {
            this.keybind = keybind;
            this.editButton = ButtonWidget.builder(Text.literal("Edit"), (button) -> {
                MinecraftClient.getInstance().setScreen(new KeybindEditorScreen(this.keybind, this.keybind.getKey()));
            }).dimensions(0, 0, 85, 20).build();
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.fill(x, y+20, x+entryWidth, y+entryHeight-20, 0xAF110A0A);
            this.editButton.setPosition(x+entryWidth/2+50, y+entryHeight/2 - 10);
            this.editButton.render(context, mouseX, mouseY, tickDelta);
            TextRenderer textRenderer = DynamicKeybindListWidget.this.client.textRenderer;
            Objects.requireNonNull(DynamicKeybindListWidget.this.client.textRenderer);

            context.drawTextWithShadow(textRenderer, Text.literal(keybind.getName()), x+10, y-5 + entryHeight / 2, -1);
        }

        public List<? extends Element> children() {
            return ImmutableList.of(this.editButton);
        }

        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.editButton);
        }
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends ElementListWidget.Entry<DynamicKeybindListWidget.Entry> {
        public Entry() {
        }
    }

    @Override
    protected int getRowTop(int index) {
        return (int) ((10 - getScrollAmount()) + index * this.itemHeight);
    }
}
