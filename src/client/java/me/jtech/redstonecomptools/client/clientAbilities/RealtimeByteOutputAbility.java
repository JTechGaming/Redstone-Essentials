package me.jtech.redstonecomptools.client.clientAbilities;

import me.jtech.redstonecomptools.SelectionData;
import me.jtech.redstonecomptools.client.RedstonecomptoolsClient;
import me.jtech.redstonecomptools.client.rendering.gui.RealtimeByteOutputRenderer;
import me.jtech.redstonecomptools.client.rendering.screen.OutputLabelInputScreen;
import me.jtech.redstonecomptools.utility.IClientSelectionContext;
import me.jtech.redstonecomptools.client.utility.Toaster;
import me.jtech.redstonecomptools.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class RealtimeByteOutputAbility extends BaseAbility implements IClientSelectionContext {
    public boolean isSelecting = false;
    RealtimeByteOutputRenderer renderer = new RealtimeByteOutputRenderer();

    public RealtimeByteOutputAbility(String identifier) {
        super("Realtime Byte", false, GLFW.GLFW_KEY_G, false, false, Identifier.of("redstonecomptools", identifier));
    }

    @Override
    public void postInit() {
        renderer.setup();
    }

    @Override
    public void toggled(boolean state) {
        if (!Config.rtbo_enabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
            shouldToast = false;
            if (!isSelecting) {
                isSelecting = true;
                Toaster.sendToast(client, Text.literal("Realtime Byte"), Text.literal("Started Creating Byte Output Selection (To cancel, press the keybind again)"));

                SelectionAbility.selectionContext = this;
            } else {
                Toaster.sendToast(client, Text.literal("Realtime Byte"), Text.literal("Cancelled Creating Byte Output Selection"));

                SelectionAbility.selectionContext = RedstonecomptoolsClient.defaultSelectionContext;
                isSelecting = false;
            }
            return;
        }
        shouldToast = true;

        RealtimeByteOutputRenderer.setShouldRender(state);
    }

    @Override
    public void used() {

    }

    @Override
    public void recall(BlockPos blockPos, Color color, Vec3i size) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (Config.use_output_labels) {
            client.setScreen(new OutputLabelInputScreen(blockPos, color, size));
        } else {
            SelectionData output = new SelectionData(blockPos, color, size, "", true);
            RealtimeByteOutputRenderer.realtimeByteOutputList.add(output);
            SelectionAbility.finalizeSelection(output);
            SelectionAbility.selectionContext = RedstonecomptoolsClient.defaultSelectionContext;
        }

        isSelecting = false;
    }
}
