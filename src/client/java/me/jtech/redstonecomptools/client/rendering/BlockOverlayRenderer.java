package me.jtech.redstonecomptools.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BlockOverlayRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final List<BlockOverlayRenderer> overlays = new ArrayList<>();

    private final BlockPos blockPos;
    private final Color color; // You can use Java's Color class or an integer for RGBA.

    public BlockOverlayRenderer(BlockPos blockPos, Color color) {
        this.blockPos = blockPos;
        this.color = color;
    }

    // Method to add an overlay
    public static void addOverlay(BlockPos blockPos, Color color) {
        overlays.add(new BlockOverlayRenderer(blockPos, color));
    }

    // Method to clear all overlays
    public static void clearOverlays() {
        overlays.clear();
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
        double x = blockPos.getX() - cameraPos.x;
        double y = blockPos.getY() - cameraPos.y;
        double z = blockPos.getZ() - cameraPos.z;

        // Define the bounding box for the block
        Box box = new Box(blockPos).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Render both outline and filled highlight
        //renderFilledBox(matrixStack, box, color, vertexConsumerProvider);
        renderOutline(matrixStack, box, color, vertexConsumerProvider);
    }

    private void renderOutline(MatrixStack matrixStack, Box box, Color color, VertexConsumerProvider vertexConsumerProvider) {
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

    private void renderFilledBox(MatrixStack matrixStack, Box box, Color color, VertexConsumerProvider vertexConsumerProvider) {
        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = 0.3f; // Set a low alpha for transparency

        // Prepare rendering system
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Disable depth testing to render through blocks
        RenderSystem.disableDepthTest();

        // Use RenderLayer.getLines() for simple geometry without UVs or normals
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
        //VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.LINES);

        MatrixStack.Entry matrixEntry = matrixStack.peek();

        // Define the vertices for each face of the box
        // Front face (example, repeat for all sides)
        vertexConsumer.vertex(matrixEntry.getPositionMatrix(), (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrixEntry.getPositionMatrix(), (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrixEntry.getPositionMatrix(), (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
        vertexConsumer.vertex(matrixEntry.getPositionMatrix(), (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);

        // Repeat for other faces (back, left, right, top, bottom)

        // Re-enable depth testing after rendering
        RenderSystem.enableDepthTest();

        // Reset the render system state
        RenderSystem.disableBlend();
    }
}
