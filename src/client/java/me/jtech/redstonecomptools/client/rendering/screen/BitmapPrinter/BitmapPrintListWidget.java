package me.jtech.redstonecomptools.client.rendering.screen.BitmapPrinter;

import com.google.common.collect.ImmutableList;
import me.jtech.redstonecomptools.SelectionData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.*;

public class BitmapPrintListWidget extends ElementListWidget<BitmapPrintListWidget.Entry> {

    public BitmapPrintListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        List<SelectionData> selections = BitmapPrinterScreen.selectionList;
        for (SelectionData selection : selections) {
            this.addEntry(new SelectionEntry(selection, Text.literal(selection.label)));
        }
    }

    public void updateChildren() {
        this.children().forEach(BitmapPrintListWidget.Entry::update);
    }

    public int getRowWidth() {
        return 420;
    }

    @Environment(EnvType.CLIENT)
    public class SelectionEntry extends BitmapPrintListWidget.Entry {
        private final Text selectionName;
        private final ButtonWidget editButton;
        private final ButtonWidget invertButton;
        private final SelectionData selection;

        SelectionEntry(final SelectionData selection, final Text selectionName) {
            this.selection = selection;
            this.selectionName = selectionName;
            this.editButton = ButtonWidget.builder(Text.literal("Edit Selection"), (button) -> {
                // TODO Edit the Selection
            }).dimensions(0, 0, 85, 20).build();
            this.invertButton = ButtonWidget.builder(Text.literal("IsInverted: " + (selection.isInverted?"Yes":"No")), (button) -> {
                selection.setInverted(!selection.isInverted);
            }).dimensions(0, 0, 85, 20).build();
            this.update();
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.fill(x, y+20, x+entryWidth, y+entryHeight-20, 0xAF110A0A);
            this.invertButton.setPosition(x+entryWidth/2, y+entryHeight/2 - 10);
            this.invertButton.setMessage(Text.literal("IsInverted: " + (selection.isInverted?"Yes":"No")));
            this.invertButton.render(context, mouseX, mouseY, tickDelta);
            this.editButton.setPosition(x+entryWidth/2+100, y+entryHeight/2 - 10);
            this.editButton.render(context, mouseX, mouseY, tickDelta);
            TextRenderer textRenderer = BitmapPrintListWidget.this.client.textRenderer;
            Objects.requireNonNull(BitmapPrintListWidget.this.client.textRenderer);

            String name = this.selectionName.getString();
            String printer = name.substring(0, name.indexOf("♅"));
            name = name.replace(printer+"♅", "");
            int channel = Integer.parseInt(name.substring(0, name.indexOf("♅")));
            String axis = name.replace(channel+"♅", "");

            if (printer.length() > 10) {
                printer = printer.substring(0, 10).concat("..");
            }

            context.drawTextWithShadow(textRenderer, Text.literal("Printer:"), x+10, y-10 + entryHeight / 2, -1); // Draw Title tag
            context.drawTextWithShadow(textRenderer, printer, x+10, y + entryHeight / 2, -1); // Draw Title data

            context.drawTextWithShadow(textRenderer, Text.literal("Channel:"), x+10 + 70, y-10 + entryHeight / 2, -1); // Draw Title tag
            context.drawTextWithShadow(textRenderer, (channel+1) + "", x+10 + 70, y + entryHeight / 2, -1); // Draw Title data

            context.drawTextWithShadow(textRenderer, Text.literal("Axis:"), x+10 + 140, y-10 + entryHeight / 2, -1); // Draw Title tag
            context.drawTextWithShadow(textRenderer, axis.toUpperCase(), x+10 + 140, y + entryHeight / 2, -1); // Draw Title data
        }

        public List<? extends Element> children() {
            return ImmutableList.of(this.editButton, this.invertButton);
        }

        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.editButton, this.invertButton);
        }

        @Override
        void update() {

        }
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends ElementListWidget.Entry<BitmapPrintListWidget.Entry> {
        public Entry() {
        }

        abstract void update();
    }
}
