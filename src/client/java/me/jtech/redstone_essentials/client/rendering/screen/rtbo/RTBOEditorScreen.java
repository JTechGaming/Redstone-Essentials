package me.jtech.redstone_essentials.client.rendering.screen.rtbo;

import me.jtech.redstone_essentials.IO.Config;
import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.rendering.BlockOverlayRenderer;
import me.jtech.redstone_essentials.client.rendering.gui.RealtimeByteOutputRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class RTBOEditorScreen extends Screen {

    private final SelectionData rtboSelection;
    private TextFieldWidget labelField;
    private TextWidget locationText;
    private TextFieldWidget colorField;
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;
    private ButtonWidget deleteButton;
    private ButtonWidget baseButton;

    private int currentBaseMode = 0;

    public RTBOEditorScreen(SelectionData rtboSelection) {
        super(Text.literal("Edit RTBO Selection"));
        this.rtboSelection = rtboSelection;
        this.currentBaseMode = rtboSelection.base;
    }

    @Override
    protected void init() {
        super.init();
        this.labelField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, 50, 200, 20, Text.literal("rtboSelection Label"));
        this.locationText = new TextWidget(this.width / 2 - 100, 75, 200, 20, Text.literal("Location"), MinecraftClient.getInstance().textRenderer);
        this.colorField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, 100, 200, 20, Text.literal("Color: #" + Integer.toHexString(rtboSelection.getColor().getRGB()).substring(2)));

        this.labelField.setText(rtboSelection.getLabel());
        BlockPos pos = rtboSelection.getBlockPos();
        this.locationText.setMessage(Text.literal("Location:   " + "x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ()));
        this.colorField.setText("Color: #" + Integer.toHexString(rtboSelection.getColor().getRGB()).substring(2));

        this.baseButton = ButtonWidget.builder(Text.literal("Base Mode: " + getEnum(currentBaseMode)), button -> {
            currentBaseMode++;
            if (currentBaseMode > 4) {
                currentBaseMode = 0;
            }
            rtboSelection.setBase(currentBaseMode);
            baseButton.setMessage(Text.literal("Base Mode: " + getEnum(currentBaseMode)));
        }).dimensions(this.width / 2 - 50, 125, 100, 20).build();

        // Delete button
        this.deleteButton = ButtonWidget.builder(Text.literal("Delete"), button -> { // Start button widget builder
            RealtimeByteOutputRenderer.realtimeByteOutputList.remove(rtboSelection);
            BlockOverlayRenderer.removeOverlayById(rtboSelection.getId());
            MinecraftClient.getInstance().setScreen(new RTBOScreen(RTBOScreen.parent)); // Go back to the list
        }).dimensions(this.width / 2 - 50, this.height - 100, 100, 20).build(); // Set dimensions and build the button

        this.addDrawableChild(deleteButton); // This one line took 2 hours to figure out lol

        // Save and Cancel buttons
        this.saveButton = ButtonWidget.builder(Text.literal("Save"), button -> { // Start button widget builder
            rtboSelection.setLabel(labelField.getText());
            Color color;
            try {
                color = Color.decode(colorField.getText().replace("Color: ", ""));
            } catch (NumberFormatException e) {
                color = rtboSelection.color;
            }

            rtboSelection.setBase(currentBaseMode);
            rtboSelection.setColor(color);
            RealtimeByteOutputRenderer.realtimeByteOutputList.set(RealtimeByteOutputRenderer.realtimeByteOutputList.indexOf(rtboSelection), rtboSelection);
            BlockOverlayRenderer.setSelection(rtboSelection.id, rtboSelection);

            MinecraftClient.getInstance().setScreen(new RTBOScreen(RTBOScreen.parent)); // Go back to the list
        }).dimensions(this.width / 2 - 50, this.height - 40, 100, 20).build(); // Set dimensions and build the button

        this.cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> { // Start button widget builder
            MinecraftClient.getInstance().setScreen(new RTBOScreen(RTBOScreen.parent)); // Go back to the list
        }).dimensions(this.width / 2 - 50, this.height - 70, 100, 20).build(); // Set dimensions and build the button

        // Add widgets
        this.addDrawableChild(labelField);
        this.addDrawableChild(locationText);
        this.addDrawableChild(colorField);
        this.addDrawableChild(baseButton);
        this.addDrawableChild(saveButton);
        this.addDrawableChild(cancelButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.labelField.render(context, mouseX, mouseY, delta);
        this.locationText.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() { //TODO comment this
        Redstone_Essentials.shouldApplyButtonStyle = false;
        this.client.setScreen(null);
    }

    public enum BaseMode {
        DEFAULT(0), HEX(1), BIN(2), DEC(3), OCT(4);
        private final int value;
        BaseMode(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public static int getInt(BaseMode enumValue) {
        return enumValue.getValue();
    }

    public static BaseMode getEnum(int value) {
        return BaseMode.values()[value];
    }
}

