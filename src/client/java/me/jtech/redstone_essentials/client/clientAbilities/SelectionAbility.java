package me.jtech.redstone_essentials.client.clientAbilities;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.rendering.BlockOverlayRenderer;
import me.jtech.redstone_essentials.client.utility.ClientSelectionHelper;
import me.jtech.redstone_essentials.IO.Config;
import me.jtech.redstone_essentials.networking.payloads.c2s.ServerSendClientPingPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class SelectionAbility extends BaseAbility {
    public static ClientSelectionHelper selectionHelper;
    public static int selectionContext = Redstone_Essentials.getInstance().DEFAULT_CONTEXT;
    public static boolean modify = false;
    public static int modificationId = 0;

    public SelectionAbility(String identifier) {
        super("Select", false, GLFW.GLFW_KEY_Y, false, false, Identifier.of("redstone_essentials", identifier));
    }

    @Override
    public void postInit() {

    }

    @Override
    public void toggled(boolean state) {
        if (!Config.selections_enabled) {
            return;
        }
        if (selectionContext == Redstone_Essentials.getInstance().DEFAULT_CONTEXT) {
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
