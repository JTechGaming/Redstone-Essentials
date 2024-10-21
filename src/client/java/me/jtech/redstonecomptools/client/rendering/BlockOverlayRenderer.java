package me.jtech.redstonecomptools.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jtech.redstonecomptools.client.rendering.gui.RealtimeByteOutputRenderer;
import me.jtech.redstonecomptools.utility.IClientSelectionContext;
import me.jtech.redstonecomptools.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BlockOverlayRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final List<BlockOverlayRenderer> overlays = new ArrayList<>();
    private static final List<BlockPos> overlayPositions = new ArrayList<>();

    private static final List<BlockOverlayRenderer> selectionOverlays = new ArrayList<>();

    public BlockPos blockPos;
    public Color color;
    public Vec3i size = new Vec3i(1, 1, 1);
    public boolean isMultiplayerPing;
    private boolean isRTBO;
    private IClientSelectionContext selectionContext;

    public BlockOverlayRenderer(BlockPos blockPos, Color color, Vec3i size, boolean isMultiplayerPing, boolean isRTBO, IClientSelectionContext selectionContext) {
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
        this.isMultiplayerPing = isMultiplayerPing;
        this.isRTBO = isRTBO;
        this.selectionContext = selectionContext;
    }

    // Method to add an overlay
    public void addOverlay(BlockPos blockPos, Color color, Vec3i size, boolean isSelectionOverlay) {
        if (blockPos == null) {
            return;
        }
        if (overlayPositions.contains(blockPos)) {
            return;
        }
        overlayPositions.add(blockPos);
        //BlockOverlayRenderer renderer = new BlockOverlayRenderer(blockPos, color, size);
        overlays.add(this);
        if (isSelectionOverlay) {
            selectionOverlays.add(this);
        }
    }

    // Method to clear all overlays
    public static void clearOverlays() {
        Object[] overlayCopy = overlays.toArray();
        for (Object r : overlayCopy) {
            BlockOverlayRenderer renderer = (BlockOverlayRenderer) r;
            if (!selectionOverlays.contains(renderer)) {
                overlayPositions.remove(renderer.blockPos);
                overlays.remove(renderer);
            }
        }
    }

    public static void clearSelectionOverlays() {
        selectionOverlays.clear();
    }

    public static void removeSelectionOverlay(BlockOverlayRenderer renderer) {
        selectionOverlays.remove(renderer);
    }

    public static void removePrinterOverlay(IClientSelectionContext context) {
        for (BlockOverlayRenderer renderer : overlays) {
            if (renderer.selectionContext == context) {
                overlays.remove(renderer);
                return;
            }
        }
    }

    // Call this method from your main rendering logic to render all overlays
    public static void renderAll(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {
        for (BlockOverlayRenderer overlay : overlays) {
            if ((!Config.pings_enabled && !selectionOverlays.contains(overlay)) || (!Config.selections_enabled && selectionOverlays.contains(overlay))) {
                continue;
            }
            if ((overlay.isRTBO && !RealtimeByteOutputRenderer.isShouldRender()) || (overlay.isRTBO && !Config.rtbo_enabled)) {
                continue;
            }
            if (!selectionOverlays.contains(overlay)) {
                overlay.color = Color.decode(overlay.isMultiplayerPing ? Config.multiplayer_ping_color : Config.ping_color);
            }
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
