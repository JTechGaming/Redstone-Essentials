package me.jtech.redstone_essentials.client.rendering.screen.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

public class PlaceholderNumberFieldWidget extends PlaceholderTextFieldWidget {
    private final String placeholder; // The placeholder text

    public PlaceholderNumberFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
        super(textRenderer, width, height, text);

        this.placeholder = text.getString();
    }

    public PlaceholderNumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);

        this.placeholder = text.getString();
    }

    public PlaceholderNumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);

        this.placeholder = text.getString();
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (NumberUtils.isParsable(String.valueOf(chr))) {
            return super.charTyped(chr, modifiers);
        } else {
            return false;
        }
    }

    @Override
    public String getText() {
        if (super.getText().equals(getPlaceholder())) return "1";
        if (super.getText().isEmpty()) return "1";
        return super.getText();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        // If the text field is empty and not focused, show the placeholder text
        if ((this.getText().isEmpty() || this.getText().equals("1")) && !this.isFocused()) {
            // Render the placeholder text in a grayed-out color
            context.drawText(MinecraftClient.getInstance().textRenderer, this.placeholder, this.getX() + 4, this.getY() + 6, 0xFF808080, false);
        }
    }

    @Override
    public String getPlaceholder() {
        return placeholder;
    }
}

