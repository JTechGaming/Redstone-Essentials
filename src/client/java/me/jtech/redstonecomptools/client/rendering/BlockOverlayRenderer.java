package me.jtech.redstonecomptools.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jtech.redstonecomptools.SelectionData;
import me.jtech.redstonecomptools.client.IO.SessionStorage;
import me.jtech.redstonecomptools.client.clientAbilities.RealtimeByteOutputAbility;
import me.jtech.redstonecomptools.client.clientAbilities.SelectionAbility;
import me.jtech.redstonecomptools.client.rendering.gui.RealtimeByteOutputRenderer;
import me.jtech.redstonecomptools.client.rendering.screen.BitmapPrinterScreen;
import me.jtech.redstonecomptools.utility.IClientSelectionContext;
import me.jtech.redstonecomptools.IO.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BlockOverlayRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static List<BlockOverlayRenderer> overlays = new ArrayList<>();
    private static List<BlockPos> overlayPositions = new ArrayList<>();

    private static List<BlockOverlayRenderer> selectionOverlays = new ArrayList<>();

    private static int offset = 0;

    public BlockPos blockPos;
    public Color color;
    public Vec3i size = new Vec3i(1, 1, 1);
    public boolean isMultiplayerPing;
    private final boolean isRTBO;
    private final int id;
    private final int clientSelectionContext;
    private String label;

    public BlockOverlayRenderer(BlockPos blockPos, Color color, Vec3i size, boolean isMultiplayerPing, boolean isRTBO, int selectionContext, String label) {
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
        this.isMultiplayerPing = isMultiplayerPing;
        this.isRTBO = isRTBO;
        this.id = getNextId();
        this.clientSelectionContext = selectionContext;
        this.label = label;
    }

    public BlockOverlayRenderer(BlockPos blockPos, Color color, Vec3i size, boolean isMultiplayerPing, boolean isRTBO, int selectionContext, String label, int id) {
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
        this.isMultiplayerPing = isMultiplayerPing;
        this.isRTBO = isRTBO;
        this.id = id;
        this.clientSelectionContext = selectionContext;
        this.label = label;
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
        if (isMultiplayerPing && isSelectionOverlay) {
            return;
        }
        saveSessions();
    }

    // Method to clear overlays
    public static void clearOverlays() {
        Object[] overlayCopy = overlays.toArray();
        for (Object r : overlayCopy) {
            BlockOverlayRenderer renderer = (BlockOverlayRenderer) r;
            if (!selectionOverlays.contains(renderer)) {
                overlayPositions.remove(renderer.blockPos);
                overlays.remove(renderer);
                offset++;
            }
        }
        saveSessions();
    }

    /**
     * This method clears all overlays and does NOT save the result to the session storage.
     * ONLY call this when destructing a session, and don't call while a session is still being written to.
     */
    public static void clearAllOverlays() {
        Object[] overlayCopy = overlays.toArray();
        for (Object r : overlayCopy) {
            BlockOverlayRenderer renderer = (BlockOverlayRenderer) r;
            overlayPositions.remove(renderer.blockPos);
            overlays.remove(renderer);
            selectionOverlays.remove(renderer);
            offset++;
        }
    }

    public static void modifySelection(int selectionId) {
        for (BlockOverlayRenderer renderer : overlays) {
            if (selectionId == renderer.id) {
                SelectionAbility.modify = true;
                SelectionAbility.modificationId = renderer.id;
                SelectionAbility.selectionContext = renderer.clientSelectionContext;
            }
        }
    }

    public static void setSelection(int selectionId, SelectionData data) {
        removeOverlayById(selectionId);
        BlockOverlayRenderer renderer = new BlockOverlayRenderer(data.blockPos, data.color, data.size, false, data.isRTBO, data.context, data.label, data.id);
        renderer.addOverlay(data.blockPos, data.color, data.size, true);
        saveSessions();
    }

    public static BlockOverlayRenderer getSelection(int id) {
        for (BlockOverlayRenderer renderer : overlays) {
            if (id == renderer.id) {
                return renderer;
            }
        }
        return null;
    }

    public static int getNextId() {
        return selectionOverlays.size() + offset;
    }

    public static void removeOverlayById(int id) {
        for (BlockOverlayRenderer renderer : overlays) {
            if (renderer.id == id) {
                overlays.remove(renderer);
                selectionOverlays.remove(renderer);
                overlayPositions.remove(renderer.blockPos);
                offset++;
                saveSessions();
                return;
            }
        }
    }

    public static void removeOverlayByContext(int context) {
        for (BlockOverlayRenderer renderer : overlays) {
            if (renderer.clientSelectionContext == context) {
                overlays.remove(renderer);
                selectionOverlays.remove(renderer);
                overlayPositions.remove(renderer.blockPos);
                offset++;
                saveSessions();
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
        renderOutline(box.stretch(new Vec3d(size.getX() - 1, size.getY() - 1, size.getZ() - 1)), color, vertexConsumerProvider);
    }

    public static void loadSessions() {
        clearAllOverlays();

        String ip;
        assert client.player != null;
        ip = "unknown";
        if (client.isInSingleplayer()) {
            IntegratedServer server = client.getServer();
            if (server != null) {
                ip = server.getSaveProperties().getLevelName();  // Get the world name
            }
        } else {
            ip = client.getCurrentServerEntry().address + "-" + client.getServer().getSaveProperties().getLevelName();
        }
        SessionStorage.Data sessionData = SessionStorage.retreiveSelectionsForServer(ip);
        if (sessionData == null) {
            return;
        }

        for (BlockOverlayRenderer renderer : sessionData.overlays) {
            overlays.add(renderer);
            if (renderer.isRTBO) {
                selectionOverlays.add(renderer);
            }
            if (renderer.isRTBO) {
                RealtimeByteOutputRenderer.realtimeByteOutputList.add(new SelectionData(renderer.blockPos, renderer.color, renderer.size, renderer.label, true));
            }
        }
        overlayPositions.addAll(sessionData.overlayPositions);
    }

    public static void saveSessions() {
        String ip;
        assert client.player != null;
        ip = "unknown";
        if (client.isInSingleplayer()) {
            IntegratedServer server = client.getServer();
            if (server != null) {
                ip = server.getSaveProperties().getLevelName();  // Get the world name
            }
        } else {
            ip = client.getCurrentServerEntry().address + "-" + client.getServer().getSaveProperties().getLevelName();
        }
        List<BlockOverlayRenderer> localOverlays = new ArrayList<>();
        List<BlockPos> localOverlayPositions = new ArrayList<>();
        List<BlockOverlayRenderer> localSelectionOverlays = new ArrayList<>();
        for (BlockOverlayRenderer renderer : overlays) {
            if (!renderer.isMultiplayerPing && renderer.clientSelectionContext != BitmapPrinterScreen.CONTEXT) {
                localOverlays.add(renderer);
                localOverlayPositions.add(renderer.blockPos);
            }
        }
        for (BlockOverlayRenderer renderer : selectionOverlays) {
            if (!renderer.isMultiplayerPing && renderer.clientSelectionContext != BitmapPrinterScreen.CONTEXT) {
                localSelectionOverlays.add(renderer);
            }
        }
        SessionStorage.storeSelectionsForServer(new SessionStorage.Data(localOverlays, localOverlayPositions, localSelectionOverlays), ip);
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
