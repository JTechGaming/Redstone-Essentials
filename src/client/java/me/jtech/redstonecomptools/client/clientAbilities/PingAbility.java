package me.jtech.redstonecomptools.client.clientAbilities;

import me.jtech.redstonecomptools.client.rendering.BlockOverlayRenderer;
import me.jtech.redstonecomptools.client.utility.RaycastingHelper;
import me.jtech.redstonecomptools.client.utility.Toaster;
import me.jtech.redstonecomptools.config.Config;
import me.jtech.redstonecomptools.networking.ServerSendClientPingPayload;
import me.jtech.redstonecomptools.networking.SetItemPayload;
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
//TODO make this send a server packet, which sends a packet to all clients to render on all clients screens
public class PingAbility extends BaseAbility {
    private boolean processedShift;

    public PingAbility(String identifier) {
        super("Ping", false, GLFW.GLFW_KEY_K, false, true, Identifier.of("redstonecomptools", identifier));
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
        BlockOverlayRenderer blockOverlayRenderer = new BlockOverlayRenderer(RaycastingHelper.performRaycast(client), Color.getColor(Config.ping_color), new Vec3i(1, 1, 1), false);
        blockOverlayRenderer.addOverlay(RaycastingHelper.performRaycast(client), Color.decode(Config.ping_color), new Vec3i(1, 1, 1), false);

        if (Config.send_pings) {
            ClientPlayNetworking.send(new ServerSendClientPingPayload(RaycastingHelper.performRaycast(client), new Vector3f(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue()), new Vector3f(1, 1, 1), false, false, ""));
        }
    }

    @Override
    public void used() {

    }
}
