package me.jtech.redstone_essentials.client.rendering.screen.keybinds;

import me.jtech.redstone_essentials.client.rendering.screen.widgets.KeybindWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class KeybindListWidget extends ScrollableWidget {

    public List<KeybindWidget> keybindWidgets = new ArrayList<>();

    public KeybindListWidget(int i, int j, int k, int l, Text text) {
        super(i, j, k, l, text);
    }

    public void addKeybind(KeybindWidget keybind) {
        keybindWidgets.add(keybind);
    }

    @Override
    protected int getContentsHeight() {
        return 0;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 0;
    }

    @Override
    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
