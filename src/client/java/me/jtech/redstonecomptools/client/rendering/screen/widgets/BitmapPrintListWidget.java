package me.jtech.redstonecomptools.client.rendering.screen.widgets;

import com.google.common.collect.ImmutableList;
import me.jtech.redstonecomptools.SelectionData;
import me.jtech.redstonecomptools.client.rendering.BlockOverlayRenderer;
import me.jtech.redstonecomptools.client.rendering.screen.BitmapPrinterScreen;
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

import java.util.*;

public class BitmapPrintListWidget extends ElementListWidget<BitmapPrintListWidget.Entry> {
    private final BitmapPrinterScreen parent;

    public BitmapPrintListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, BitmapPrinterScreen parent) {
        super(client, width, height, y, itemHeight);
        this.parent = parent;
        List<SelectionData> selections = BitmapPrinterScreen.selectionList;
        for (SelectionData selection : selections) {
            this.addEntry(new SelectionEntry(selection, Text.literal(selection.label)));
        }
    }

    public int getRowWidth() {
        return 500;
    }

    @Environment(EnvType.CLIENT)
    public class SelectionEntry extends BitmapPrintListWidget.Entry {
        private final Text selectionName;
        private final ButtonWidget editButton;
        private final ButtonWidget invertButton;
        private final SelectionData selection;
        private final PlaceholderTextFieldWidget offsetField;

        SelectionEntry(final SelectionData selection, final Text selectionName) {
            this.selection = selection;
            this.selectionName = selectionName;
            this.offsetField = new PlaceholderTextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 85, 20, Text.literal("Offset"));
            offsetField.setMessage(Text.literal("1"));
            this.editButton = ButtonWidget.builder(Text.literal("Edit Selection"), (button) -> {
//                BlockOverlayRenderer.modifySelection(selection.id);
//                BitmapPrinterScreen.currentGuiText = "Selecting Bitmap Printer Input For Channel " + ((BitmapPrinterScreen.completedSelections / 2) + 1) + " On The " + (((BitmapPrinterScreen.completedSelections & 1) == 0) ? "X Axis" : "Y Axis");
//                parent.shouldClose = true;
//                BitmapPrinterScreen.shouldRender = true;
//                parent.close();
            }).dimensions(0, 0, 85, 20).build();
            editButton.active = false;
            editButton.setTooltip(Tooltip.of(Text.literal("(Coming in 1.1) Edit this selection")));
            this.invertButton = ButtonWidget.builder(Text.literal("IsInverted: " + (selection.isInverted?"Yes":"No")), (button) -> {
                selection.setInverted(!selection.isInverted);
            }).dimensions(0, 0, 85, 20).build();
            invertButton.setTooltip(Tooltip.of(Text.literal("Invert the byte direction (default least significant on north or west)")));
            offsetField.setTooltip(Tooltip.of(Text.literal("(Coming in 1.1) The offset between bits")));
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.fill(x, y+20, x+entryWidth, y+entryHeight-20, 0xAF110A0A);
            this.offsetField.setPosition(x+entryWidth/2-30, y+entryHeight/2 - 10);
            this.offsetField.render(context, mouseX, mouseY, tickDelta);
            this.invertButton.setPosition(x+entryWidth/2+50, y+entryHeight/2 - 10);
            this.invertButton.setMessage(Text.literal("IsInverted: " + (selection.isInverted?"Yes":"No")));
            this.invertButton.render(context, mouseX, mouseY, tickDelta);
            this.editButton.setPosition(x+entryWidth/2+100+50, y+entryHeight/2 - 10);
            this.editButton.render(context, mouseX, mouseY, tickDelta);
            TextRenderer textRenderer = BitmapPrintListWidget.this.client.textRenderer;
            Objects.requireNonNull(BitmapPrintListWidget.this.client.textRenderer);

            //BitmapPrinterScreen.setOffset(selection, Integer.parseInt(offsetField.getText()));
            offsetField.setEditable(true);
            offsetField.active = true;
            offsetField.visible = true;

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
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends ElementListWidget.Entry<BitmapPrintListWidget.Entry> {
        public Entry() {
        }
    }
}
