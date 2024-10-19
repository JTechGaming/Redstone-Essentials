package me.jtech.redstonecomptools.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockOverlayRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final List<BlockOverlayRenderer> overlays = new ArrayList<>();
    private static final List<BlockPos> overlayPositions = new ArrayList<>();

    private final BlockPos blockPos;
    private final Color color; // You can use Java's Color class or an integer for RGBA.
    private Vec3i size = new Vec3i(1, 1, 1);

    public BlockOverlayRenderer(BlockPos blockPos, Color color, Vec3i size) {
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
    }

    // Method to add an overlay
    public static void addOverlay(BlockPos blockPos, Color color, Vec3i size) {
        if (blockPos == null) {
            return;
        }
        if (overlayPositions.contains(blockPos)) {
            return;
        }
        overlayPositions.add(blockPos);
        overlays.add(new BlockOverlayRenderer(blockPos, color, size));
    }

    // Method to clear all overlays
    public static void clearOverlays() {
        overlays.clear();
        overlayPositions.clear();
    }

    // Call this method from your main rendering logic to render all overlays
    public static void renderAll(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {
        for (BlockOverlayRenderer overlay : overlays) {
            overlay.render(matrixStack, vertexConsumerProvider);
        }
    }

    // Rendering logic for the overlay around a specific block
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        // Define the bounding box for the block
        Box box = new Box(blockPos).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Render both outline and filled highlight
        renderOutline(box.stretch(new Vec3d(size.getX()-1, size.getY()-1, size.getZ()-1)), color, vertexConsumerProvider);
    }

    private void renderOutline(Box box, Color color, VertexConsumerProvider vertexConsumerProvider) {
        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = color.getAlpha() / 255.0f;

        // Prepare rendering system
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f); // Set the width of the outline

        // Disable depth testing to make the outline render through blocks
        RenderSystem.disableDepthTest();

        // Render the box outline
        WorldRenderer.drawBox(vertexConsumerProvider.getBuffer(RenderLayer.LINES),
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                red, green, blue, alpha);

        // Re-enable depth testing after rendering
        RenderSystem.enableDepthTest();

        // Reset the render system state
        RenderSystem.disableBlend();
    }
}
