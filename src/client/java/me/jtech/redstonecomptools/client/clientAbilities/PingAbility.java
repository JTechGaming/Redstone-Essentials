package me.jtech.redstonecomptools.client.clientAbilities;

import me.jtech.redstonecomptools.client.rendering.BlockOverlay;
import me.jtech.redstonecomptools.client.utility.RaycastingHelper;
import me.jtech.redstonecomptools.client.utility.Toaster;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class PingAbility extends BaseAbility{ //TODO comment this
    public PingAbility(String identifier) {
        super("Ping", false, GLFW.GLFW_KEY_K, false, true, Identifier.of(identifier));
    }

    @Override
    public void postInit() {

    }

    @Override
    public void toggled(boolean state) { //TODO comment this
        // Adding an overlay
        MinecraftClient client = MinecraftClient.getInstance();

        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
            BlockOverlay.clearOverlays();
            Toaster.sendToast(client, Text.literal("Ping"), Text.literal("Removed all pings"));
            return;
        }

        BlockOverlay.addOverlay(RaycastingHelper.performRaycast(client), Color.RED);

        // Clearing all overlays
        //BlockOverlay.clearOverlays();
    }

    @Override
    public void used() {

    }
}
