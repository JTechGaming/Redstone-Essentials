package me.jtech.redstone_essentials.client.clientAbilities;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.rendering.gui.RealtimeByteOutputRenderer;
import me.jtech.redstone_essentials.client.rendering.screen.OutputLabelInputScreen;
import me.jtech.redstone_essentials.utility.IClientSelectionContext;
import me.jtech.redstone_essentials.client.utility.Toaster;
import me.jtech.redstone_essentials.IO.Config;
import me.jtech.redstone_essentials.utility.SelectionContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class RealtimeByteOutputAbility extends BaseAbility implements IClientSelectionContext {
    public static int CONTEXT = SelectionContext.register(new RealtimeByteOutputAbility("realtime_byte_output"));

    public static boolean isSelecting = false;
    private static final RealtimeByteOutputRenderer renderer = new RealtimeByteOutputRenderer();

    public RealtimeByteOutputAbility(String identifier) {
        super("Realtime Byte", false, GLFW.GLFW_KEY_G, false, false, Identifier.of("redstone_essentials", identifier));
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

                SelectionAbility.selectionContext = CONTEXT;
            } else {
                Toaster.sendToast(client, Text.literal("Realtime Byte"), Text.literal("Cancelled Creating Byte Output Selection"));

                SelectionAbility.selectionContext = Redstone_Essentials.getInstance().DEFAULT_CONTEXT;
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
    public void recall(BlockPos blockPos, Color color, Vec3i size, int id, boolean wasModified) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (Config.use_output_labels) {
            client.setScreen(new OutputLabelInputScreen(blockPos, color, size));
        } else {
            SelectionData output = new SelectionData(blockPos, color, size, "", true, client.player.getUuid().toString());
            RealtimeByteOutputRenderer.realtimeByteOutputList.add(output);
            SelectionAbility.finalizeSelection(output);
            SelectionAbility.selectionContext = Redstone_Essentials.getInstance().DEFAULT_CONTEXT;
        }

        isSelecting = false;
    }
}
