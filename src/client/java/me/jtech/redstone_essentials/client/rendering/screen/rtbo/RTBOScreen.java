package me.jtech.redstone_essentials.client.rendering.screen.rtbo;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.client.rendering.screen.widgets.DynamicKeybindListWidget;
import me.jtech.redstone_essentials.client.rendering.screen.widgets.RTBOSelectionsListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class RTBOScreen extends Screen {
    public static Screen parent;

    private RTBOSelectionsListWidget scrollWidget;

    public RTBOScreen(Screen parent) {
        super(Text.literal("RTBO Selections"));
        Redstone_Essentials.shouldApplyButtonStyle = true;
        RTBOScreen.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        addDrawableChild(new TextWidget(this.width/2-100, -15, 200, 50, Text.literal("RTBO Selections"), client.textRenderer));

        this.scrollWidget = new RTBOSelectionsListWidget(client, this.width, 300, 20, 80);

        // Add the button to the screen
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

    public void updateRTBOSelections() {
        init(); // Reinitialize the screen to reflect changes
    }

    @Override
    public <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        return super.addDrawableChild(drawableElement);
    }
}
