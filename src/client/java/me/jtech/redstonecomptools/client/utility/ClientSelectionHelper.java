package me.jtech.redstonecomptools.client.utility;

import me.jtech.redstonecomptools.client.clientAbilities.RealtimeByteOutputAbility;
import me.jtech.redstonecomptools.client.rendering.BlockOverlayRenderer;
import me.jtech.redstonecomptools.utility.IClientSelectionContext;
import me.jtech.redstonecomptools.utility.SelectionContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientSelectionHelper {
    public static List<ClientSelectionHelper> clientSelectionHelpers = new ArrayList<>();

    public BlockOverlayRenderer renderer;
    private boolean pos1Selected = false;

    private final int selectionContext;

    private final boolean modify;
    public final int modificationId;

    public ClientSelectionHelper(int selectionContext, boolean modify, int modificationId) {
        this.selectionContext = selectionContext;
        this.modify = modify;
        this.modificationId = modificationId;

        Random random = new Random();
        Color randomColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));

        Vec3i area = new Vec3i(1, 1, 1);
        renderer = new BlockOverlayRenderer(new BlockPos(0, 0, 0), randomColor, area, false, selectionContext == RealtimeByteOutputAbility.CONTEXT, selectionContext, "");

        clientSelectionHelpers.add(this);
    }

    public void startSelection() {
        if (pos1Selected || renderer == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        BlockPos blockPos = RaycastingHelper.performRaycast(client);
        if (blockPos==null) {
            return;
        }
        renderer.blockPos = blockPos;
        renderer.addOverlay(blockPos, renderer.color, renderer.size, true);

        pos1Selected = true;
    }

    public Vec3i endSelection() {
        if (!pos1Selected || renderer == null) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        BlockPos blockPos = RaycastingHelper.performRaycast(client);
        if (blockPos==null) {
            return null;
        }

        renderer.size = calculateAreaForPositions(renderer.blockPos, blockPos);

        int id = BlockOverlayRenderer.getNextId();
        if (modify) {
            id = modificationId;
        }

        SelectionContext.get(selectionContext).recall(renderer.blockPos, renderer.color, renderer.size, id, modify);

        return renderer.size;
    }

    public void renderSelection() {
        if (!pos1Selected || renderer == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        BlockPos blockPos = RaycastingHelper.performRaycast(client);
        if (blockPos==null) {
            return;
        }

        renderer.size = calculateAreaForPositions(renderer.blockPos, blockPos);
    }

    public Vec3i calculateAreaForPositions(BlockPos pos1, BlockPos pos2) {
        int x = pos2.getX()-pos1.getX() + 1;
        int y = pos2.getY()-pos1.getY() + 1;
        int z = pos2.getZ()-pos1.getZ() + 1;
        return new Vec3i(x, y, z);
    }

    public static void renderAll() {
        for (ClientSelectionHelper clientSelectionHelper : clientSelectionHelpers) {
            clientSelectionHelper.renderSelection();
        }
    }
}
