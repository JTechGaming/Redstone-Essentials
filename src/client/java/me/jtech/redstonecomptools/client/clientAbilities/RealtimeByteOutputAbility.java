package me.jtech.redstonecomptools.client.clientAbilities;

import me.jtech.redstonecomptools.client.gui.RealtimeByteOutputRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class RealtimeByteOutputAbility extends BaseAbility {
    public RealtimeByteOutputAbility(String identifier) {
        super("Realtime Byte", false, GLFW.GLFW_KEY_G, true, false, Identifier.of("redstonecomptools", identifier));
    }

    @Override
    public void postInit() {
        RealtimeByteOutputRenderer.setup();
    }

    @Override
    public void toggled(boolean state) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
            return;
        }

        RealtimeByteOutputRenderer.setShouldRender(state);
    }

    @Override
    public void used() {

    }
}
