package me.jtech.redstone_essentials.client.clientAbilities;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.client.rendering.BlockOverlayRenderer;
import me.jtech.redstone_essentials.client.utility.RaycastingHelper;
import me.jtech.redstone_essentials.client.utility.Toaster;
import me.jtech.redstone_essentials.IO.Config;
import me.jtech.redstone_essentials.networking.payloads.c2s.ServerSendClientPingPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

//TODO comment this
public class PingAbility extends BaseAbility {
    private boolean processedShift;

    public PingAbility(String identifier) {
        super("Ping", false, GLFW.GLFW_KEY_K, false, true, Identifier.of("redstone_essentials", identifier));
    }

    @Override
    public void postInit() {

    }

    @Override
    public void toggled(boolean state) { //TODO comment this
        // Adding an overlay
        MinecraftClient client = MinecraftClient.getInstance();
        if (!Config.pings_enabled) {
            return;
        }
        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT) && !processedShift) {
            processedShift = true;
            BlockOverlayRenderer.clearOverlays();
            Toaster.sendToast(client, Text.literal("Ping"), Text.literal("Removed all pings"));
            return;
        } else {
            processedShift = false;
        }

        if (RaycastingHelper.performRaycast(client) == null) {
            return;
        }
        BlockOverlayRenderer blockOverlayRenderer = new BlockOverlayRenderer(RaycastingHelper.performRaycast(client), Color.getColor(Config.ping_color), new Vec3i(1, 1, 1), false, false, Redstone_Essentials.getInstance().DEFAULT_CONTEXT, "");
        blockOverlayRenderer.addOverlay(RaycastingHelper.performRaycast(client), Color.decode(Config.ping_color), new Vec3i(1, 1, 1), false);

        if (Config.send_pings) {
            ClientPlayNetworking.send(new ServerSendClientPingPayload(RaycastingHelper.performRaycast(client), new Vector3f(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue()), new Vector3f(1, 1, 1), false, false, ""));
        }
    }

    @Override
    public void used() {

    }
}
