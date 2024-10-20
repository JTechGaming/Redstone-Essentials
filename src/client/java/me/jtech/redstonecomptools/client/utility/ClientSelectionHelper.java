package me.jtech.redstonecomptools.client.utility;

import me.jtech.redstonecomptools.client.RedstonecomptoolsClient;
import me.jtech.redstonecomptools.client.rendering.BlockOverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientSelectionHelper {
    public static List<ClientSelectionHelper> clientSelectionHelpers = new ArrayList<>();

    public BlockOverlayRenderer renderer;
    private boolean pos1Selected = false;

    private final IClientSelectionContext selectionContext;

    public ClientSelectionHelper(IClientSelectionContext selectionContext) {
        this.selectionContext = selectionContext;

        Random random = new Random();
        Color randomColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));

        Vec3i area = new Vec3i(1, 1, 1);
        renderer = new BlockOverlayRenderer(new BlockPos(0, 0, 0), randomColor, area, false);

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

        selectionContext.recall(renderer.blockPos, renderer.color, renderer.size);

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
        /*int x = (pos2.getX() > pos1.getX() ? pos2.getX()-pos1.getX() : pos1.getX()-pos2.getX()) + 1;
        int y = (pos2.getY() > pos1.getY() ? pos2.getY()-pos1.getY() : pos1.getY()-pos2.getY()) + 1;
        int z = (pos2.getZ() > pos1.getZ() ? pos2.getZ()-pos1.getZ() : pos1.getZ()-pos2.getZ()) + 1;*/
        int x = pos2.getX()-pos1.getX() + 1;
        int y = pos2.getY()-pos1.getY() + 1;
        int z = pos2.getZ()-pos1.getZ() + 1;
        return new Vec3i(x, y, z);
    }

    public static Box evaluateSelection(BlockPos pos1, Vec3i size) {
        BlockPos pos2 = pos1.add(size);
        return new Box(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    public static void renderAll() {
        for (ClientSelectionHelper clientSelectionHelper : clientSelectionHelpers) {
            clientSelectionHelper.renderSelection();
        }
    }
}
