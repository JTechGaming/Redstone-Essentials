package me.jtech.redstone_essentials.client.rendering.screen.widgets;

import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindEditorScreen;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindScreen;
import me.jtech.redstone_essentials.client.rendering.screen.rtbo.RTBOEditorScreen;
import me.jtech.redstone_essentials.client.rendering.screen.rtbo.RTBOScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class RTBOWidget extends ButtonWidget { //TODO comment this
    private final SelectionData selection;
    private boolean expanded = false;
    private RTBOScreen parentScreen;

    private ButtonWidget button;

    // Constructor using the default narration supplier
    public RTBOWidget(RTBOScreen parentScreen, SelectionData selection, int x, int y, int width, int height) {
        super(x, y, width, height, Text.literal(selection.getLabel()), button -> {}, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.selection = selection;
        this.parentScreen = parentScreen;
    }

    @Override
    public void onPress() {
        // Toggle expanded state on click
        expanded = !expanded;
        if (expanded) {
            // Render the button for editing the selection
            button = this.parentScreen.addDrawableChild(ButtonWidget.builder(Text.literal("Edit"), b -> {
                // Open RTBOEditorScreen for this selection
                MinecraftClient.getInstance().setScreen(new RTBOEditorScreen(this.selection));
            }).dimensions(this.getX() + 310, this.getY()+(this.height/2), 80, 20).tooltip(Tooltip.of(Text.literal("Edit this rtbo's settings"))).build());

            // TODO Add additional UI elements for properties
        } else {
            // Collapse the widget by removing extra buttons
            parentScreen.updateRTBOSelections();
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        // If expanded, render more details like properties
        if (expanded) {
            // Toggle button visibility
            if (button != null) {
                button.visible = true;
            }
            //context.fill(this.getX()+this.width, this.getY(), this.getX()+this.width+200, this.getY()+this.height*2, 0xAF221B1B); //0x221B1B
            //context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Command: " + selection.getCommand(), this.getX() + 205, this.getY()+this.height-5, 0xFFFFFF);
        } else {
            // Toggle button visibility
            if (button != null) {
                button.visible = false;
            }
        }
    }
}

