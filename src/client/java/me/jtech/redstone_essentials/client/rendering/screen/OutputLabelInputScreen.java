package me.jtech.redstone_essentials.client.rendering.screen;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.clientAbilities.SelectionAbility;
import me.jtech.redstone_essentials.client.rendering.BlockOverlayRenderer;
import me.jtech.redstone_essentials.client.rendering.gui.RealtimeByteOutputRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

public class OutputLabelInputScreen extends Screen {
    private TextFieldWidget nameField;
    private ButtonWidget confirmButton;
    private final BlockPos blockPos;
    private final Color color;
    private final Vec3i size;

    public OutputLabelInputScreen(BlockPos blockPos, Color color, Vec3i size) {
        super(Text.literal("Set Output Label"));
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
    }

    @Override
    protected void init() {
        super.init();
        this.nameField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, this.height/2, 200, 20, Text.literal("Output Label"));
        this.confirmButton = ButtonWidget.builder(Text.literal("Confirm"), button -> {
            SelectionData output = new SelectionData(blockPos, color, size, nameField.getText(), true);
            RealtimeByteOutputRenderer.realtimeByteOutputList.add(output);
            BlockOverlayRenderer renderer = BlockOverlayRenderer.getSelection(output.id);
            if (renderer != null) {
                renderer.setLabel(output.label);
            }
            SelectionAbility.finalizeSelection(output);
            SelectionAbility.selectionContext = Redstone_Essentials.getInstance().DEFAULT_CONTEXT;
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(this.width / 2 - 100, this.height/2+40, 200, 20).build();

        this.addDrawableChild(nameField);
        this.addDrawableChild(confirmButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.nameField.render(context, mouseX, mouseY, delta);
    }
}
