package me.jtech.redstone_essentials.client.rendering.screen.widgets;

import com.google.common.collect.ImmutableList;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.client.rendering.gui.RealtimeByteOutputRenderer;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindEditorScreen;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindEntry;
import me.jtech.redstone_essentials.client.rendering.screen.rtbo.RTBOEditorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public class RTBOSelectionsListWidget extends ElementListWidget<RTBOSelectionsListWidget.Entry> {
    public RTBOSelectionsListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        List<SelectionData> selections = RealtimeByteOutputRenderer.realtimeByteOutputList;
        for (SelectionData selection : selections) {
            this.addEntry(new SelectionEntry(selection));
        }
    }

    public int getRowWidth() {
        return 300;
    }

    @Environment(EnvType.CLIENT)
    public class SelectionEntry extends RTBOSelectionsListWidget.Entry {
        private final ButtonWidget editButton;
        private final SelectionData selection;

        SelectionEntry(final SelectionData selection) {
            this.selection = selection;
            this.editButton = ButtonWidget.builder(Text.literal("Edit"), (button) -> {
                Redstone_Essentials_Client.LOGGER.info("Opening RTBO Editor for " + selection.getLabel());
                Screen screen = new RTBOEditorScreen(selection);
                MinecraftClient.getInstance().setScreen(screen);
            }).dimensions(0, 0, 85, 20).build();
            Redstone_Essentials_Client.LOGGER.info("Created selection entry for " + selection.getLabel());
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.fill(x, y+20, x+entryWidth, y+entryHeight-20, 0xAF110A0A);
            this.editButton.setPosition(x+entryWidth/2+50, y+entryHeight/2 - 10);
            this.editButton.render(context, mouseX, mouseY, tickDelta);
            TextRenderer textRenderer = RTBOSelectionsListWidget.this.client.textRenderer;
            Objects.requireNonNull(RTBOSelectionsListWidget.this.client.textRenderer);
            String name = selection.getLabel().isBlank() ? "Unnamed Selection" : selection.getLabel();
            context.drawTextWithShadow(textRenderer, Text.literal(name), x+10, y-5 + entryHeight / 2, -1);
        }

        public List<? extends Element> children() {
            return List.of(this.editButton);
        }

        public List<? extends Selectable> selectableChildren() {
            return List.of(this.editButton);
        }
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends ElementListWidget.Entry<RTBOSelectionsListWidget.Entry> {
        public Entry() {
        }
    }

    @Override
    protected int getRowTop(int index) {
        return (int) ((10 - getScrollAmount()) + index * this.itemHeight);
    }
}
