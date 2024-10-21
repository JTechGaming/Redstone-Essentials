package me.jtech.redstonecomptools.client.rendering.screen.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PlaceholderTextFieldWidget extends TextFieldWidget {
    private final String placeholder; // The placeholder text

    public PlaceholderTextFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
        super(textRenderer, width, height, text);

        this.placeholder = text.getString();
    }

    public PlaceholderTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);

        this.placeholder = text.getString();
    }

    public PlaceholderTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);

        this.placeholder = text.getString();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        // If the text field is empty and not focused, show the placeholder text
        if (this.getText().isEmpty() && !this.isFocused()) {
            // Render the placeholder text in a grayed-out color
            context.drawText(MinecraftClient.getInstance().textRenderer, this.placeholder, this.getX() + 4, this.getY() + 6, 0xFF808080, false);
        }
    }

    @Override
    public String getText() {
        return super.getText().equals(getPlaceholder()) ? "" : super.getText();
    }

    public String getPlaceholder() {
        return placeholder;
    }
}

