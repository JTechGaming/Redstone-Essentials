package me.jtech.redstonecomptools.client.clientAbilities;

import me.jtech.redstonecomptools.Redstonecomptools;
import me.jtech.redstonecomptools.SelectionData;
import me.jtech.redstonecomptools.client.RedstonecomptoolsClient;
import me.jtech.redstonecomptools.client.rendering.BlockOverlayRenderer;
import me.jtech.redstonecomptools.client.utility.ClientSelectionHelper;
import me.jtech.redstonecomptools.utility.IClientSelectionContext;
import me.jtech.redstonecomptools.IO.Config;
import me.jtech.redstonecomptools.networking.payloads.c2s.ServerSendClientPingPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class SelectionAbility extends BaseAbility {
    public static ClientSelectionHelper selectionHelper;
    public static int selectionContext = Redstonecomptools.getInstance().DEFAULT_CONTEXT;
    public static boolean modify = false;
    public static int modificationId = 0;

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
        if (selectionContext == Redstonecomptools.getInstance().DEFAULT_CONTEXT) {
            return;
        }
        if (selectionHelper == null) {
            selectionHelper = new ClientSelectionHelper(selectionContext, modify, modificationId);
            selectionHelper.startSelection();
        } else {
            Vec3i selection = selectionHelper.endSelection();
            if (selection != null) {
                BlockOverlayRenderer blockOverlayRenderer = new BlockOverlayRenderer(selectionHelper.renderer.blockPos, selectionHelper.renderer.color, selection, false, selectionContext == RealtimeByteOutputAbility.CONTEXT, selectionContext, "");
                blockOverlayRenderer.addOverlay(selectionHelper.renderer.blockPos, selectionHelper.renderer.color, selection, true);

                selectionHelper.renderer = null;
                ClientSelectionHelper.clientSelectionHelpers.remove(selectionHelper);

                selectionHelper = null;
            }
        }
    }

    public static void finalizeSelection(SelectionData output) {
        if (!(selectionContext == RealtimeByteOutputAbility.CONTEXT) && Config.send_selections) {
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
        } else if ((selectionContext == RealtimeByteOutputAbility.CONTEXT) && Config.send_rtbo) {
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
