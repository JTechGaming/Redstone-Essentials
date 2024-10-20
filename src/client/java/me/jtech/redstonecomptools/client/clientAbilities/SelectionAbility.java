package me.jtech.redstonecomptools.client.clientAbilities;

import me.jtech.redstonecomptools.RealtimeByteOutput;
import me.jtech.redstonecomptools.client.RedstonecomptoolsClient;
import me.jtech.redstonecomptools.client.rendering.BlockOverlayRenderer;
import me.jtech.redstonecomptools.client.utility.ClientSelectionHelper;
import me.jtech.redstonecomptools.client.utility.IClientSelectionContext;
import me.jtech.redstonecomptools.config.Config;
import me.jtech.redstonecomptools.networking.ServerSendClientPingPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class SelectionAbility extends BaseAbility {
    public static ClientSelectionHelper selectionHelper;
    public static IClientSelectionContext selectionContext = RedstonecomptoolsClient.defaultSelectionContext;

    public SelectionAbility(String identifier) {
        super("Select", false, GLFW.GLFW_KEY_Y, false, false, Identifier.of("redstonecomptools", identifier));
    }

    @Override
    public void postInit() {

    }

    @Override
    public void toggled(boolean state) {
        if (!Config.selections_enabled) {
            return;
        }
        if (selectionContext == RedstonecomptoolsClient.defaultSelectionContext) {
            return;
        }
        if (selectionHelper == null) {
            selectionHelper = new ClientSelectionHelper(selectionContext);
            selectionHelper.startSelection();
        } else {
            Vec3i selection = selectionHelper.endSelection();
            if (selection != null) {
                BlockOverlayRenderer blockOverlayRenderer = new BlockOverlayRenderer(selectionHelper.renderer.blockPos, selectionHelper.renderer.color, selection, false);
                blockOverlayRenderer.addOverlay(selectionHelper.renderer.blockPos, selectionHelper.renderer.color, selection, true);

                selectionHelper.renderer = null;
                ClientSelectionHelper.clientSelectionHelpers.remove(selectionHelper);

                selectionHelper = null;
            }
        }
    }

    public static void finalizeSelection(RealtimeByteOutput output) {
        if (!(selectionContext instanceof RealtimeByteOutputAbility) && Config.send_selections) {
            ClientPlayNetworking.send(new ServerSendClientPingPayload(
                    output.blockPos,
                    new Vector3f(
                            output.color.getRed(),
                            output.color.getGreen(),
                            output.color.getBlue()),
                    new Vector3f(output.size.getX(), output.size.getY(), output.size.getZ()),
                    true,
                    false,
                    output.label)
            );
        } else if ((selectionContext instanceof RealtimeByteOutputAbility) && Config.send_rtbo) {
            ClientPlayNetworking.send(new ServerSendClientPingPayload(
                    output.blockPos,
                    new Vector3f(
                            output.color.getRed(),
                            output.color.getGreen(),
                            output.color.getBlue()),
                    new Vector3f(output.size.getX(), output.size.getY(), output.size.getZ()),
                    true,
                    true,
                    output.label)
            );
        }
    }

    @Override
    public void used() {

    }
}
