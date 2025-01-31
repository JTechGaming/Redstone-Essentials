package me.jtech.redstone_essentials.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.IO.SessionStorage;
import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.client.clientAbilities.SelectionAbility;
import me.jtech.redstone_essentials.client.rendering.gui.RealtimeByteOutputRenderer;
import me.jtech.redstone_essentials.client.rendering.screen.BitmapPrinterScreen;
import me.jtech.redstone_essentials.IO.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.joml.Matrix4f;

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
    public final boolean isRTBO;
    private final int id;
    private final int clientSelectionContext;
    private String label;
    public  String owningPlayer;

    public BlockOverlayRenderer(BlockPos blockPos, Color color, Vec3i size, boolean isMultiplayerPing, boolean isRTBO, int selectionContext, String label, String owningPlayer) {
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
        this.isMultiplayerPing = isMultiplayerPing;
        this.isRTBO = isRTBO;
        this.id = getNextId();
        this.clientSelectionContext = selectionContext;
        this.label = label;
        this.owningPlayer = owningPlayer;
    }

    public BlockOverlayRenderer(BlockPos blockPos, Color color, Vec3i size, boolean isMultiplayerPing, boolean isRTBO, int selectionContext, String label, int id, String owningPlayer) {
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
        this.isMultiplayerPing = isMultiplayerPing;
        this.isRTBO = isRTBO;
        this.id = id;
        this.clientSelectionContext = selectionContext;
        this.label = label;
        this.owningPlayer = owningPlayer;
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
        this.color = color;
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

    public static void clearOverlays(String player) {
        Object[] overlayCopy = overlays.toArray();
        for (Object r : overlayCopy) {
            BlockOverlayRenderer renderer = (BlockOverlayRenderer) r;
            if (renderer.owningPlayer.equalsIgnoreCase(player)) {
                overlayPositions.remove(renderer.blockPos);
                overlays.remove(renderer);
                offset++;
            }
        }
        saveSessions();
    }

    // Method to clear overlays
    public static void clearLocalPlayerOverlays() {
        Object[] overlayCopy = overlays.toArray();
        for (Object r : overlayCopy) {
            BlockOverlayRenderer renderer = (BlockOverlayRenderer) r;
            if (!selectionOverlays.contains(renderer) && !renderer.isMultiplayerPing) {
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
        overlays.clear();
        overlayPositions.clear();
        selectionOverlays.clear();

        RealtimeByteOutputRenderer.realtimeByteOutputList.clear();
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

    public static void changeSelectionLabel(int id, String label) {
        for (BlockOverlayRenderer renderer : overlays) {
            if (id == renderer.id) {
                renderer.setLabel(label);
            }
        }
        saveSessions();
    }

    public static void setSelection(int selectionId, SelectionData data) {
        removeOverlayById(selectionId);
        BlockOverlayRenderer renderer = new BlockOverlayRenderer(data.blockPos, data.color, data.size, false, data.isRTBO, data.context, data.label, data.id, data.owningPlayer);
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
            overlay.render(matrixStack, vertexConsumerProvider);
        }
    }

    // Rendering logic for the overlay around a specific block
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        // Define the bounding box for the block
        Box box = new Box(blockPos).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Render both outline and filled highlight
        renderOutline(box.stretch(new Vec3d(size.getX() - 1, size.getY() - 1, size.getZ() - 1)), color, vertexConsumerProvider, matrixStack);
    }

    public static void loadSessions() {
        clearAllOverlays();

        String ip = "unknown";
        assert client.player != null;
        if (client.isInSingleplayer()) {
            IntegratedServer server = client.getServer();
            if (server != null) {
                ip = server.getSaveProperties().getLevelName();  // Get the world name
            }
        } else {
            if (client.world != null) {
                RegistryKey<World> dimensionKey = client.world.getRegistryKey();
                ip = client.getCurrentServerEntry().address + "-" + dimensionKey.getValue().getPath();
            }
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
                RealtimeByteOutputRenderer.realtimeByteOutputList.add(new SelectionData(renderer.blockPos, renderer.color, renderer.size, renderer.label, true, renderer.owningPlayer));
            }
        }
        overlayPositions.addAll(sessionData.overlayPositions);
    }

    public static void saveSessions() {
        String ip = "unknown";
        assert client.player != null;
        if (client.isInSingleplayer()) {
            IntegratedServer server = client.getServer();
            if (server != null) {
                ip = server.getSaveProperties().getLevelName();  // Get the world name
            }
        } else {
            if (client.world != null) {
                RegistryKey<World> dimensionKey = client.world.getRegistryKey();
                ip = client.getCurrentServerEntry().address + "-" + dimensionKey.getValue().getPath();
            }
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

    private void renderOutline(Box box, Color color, VertexConsumerProvider vertexConsumerProvider, MatrixStack matrixStack) {
        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = color.getAlpha() / 255.0f;

        Matrix4f transformationMatrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(20.0f);
        if (!Config.ping_skip_depth_test) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // Top Face Edges
        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);

        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);

        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);

        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);

        // Vertical Edges
        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);

        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);

        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);

        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);

        // Bottom Face Edges
        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);

        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);

        buffer.vertex(transformationMatrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);

        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(transformationMatrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        if (Config.ping_render_faces) {
            renderTest(box, matrixStack);
        }
    }

    private void renderTest(Box box, MatrixStack matrixStack) {
        Matrix4f transformationMatrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (!Config.ping_skip_depth_test) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        // Define the six faces of the cube
        // Bottom face (minY)
        addFace(buffer, transformationMatrix, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, 0.5f, 0.0f, 0.0f, 0.4f);

        // Top face (maxY)
        addFace(buffer, transformationMatrix, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, 0.5f, 0.0f, 0.0f, 0.4f);

        // Front face (maxZ)
        addFace(buffer, transformationMatrix, box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, 0.5f, 0.0f, 0.0f, 0.4f);

        // Back face (minZ)
        addFace(buffer, transformationMatrix, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, 0.5f, 0.0f, 0.0f, 0.4f);

        // Left face (minX)
        addFace(buffer, transformationMatrix, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ, 0.5f, 0.0f, 0.0f, 0.4f);

        // Right face (maxX)
        addFace(buffer, transformationMatrix, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 0.5f, 0.0f, 0.0f, 0.4f);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Clean up after rendering
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    // Helper method to add a face
    private void addFace(BufferBuilder buffer, Matrix4f matrix, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        // Define two triangles for the face
        buffer.vertex(matrix, (float) minX, (float) minY, (float) minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a);

        buffer.vertex(matrix, (float) minX, (float) minY, (float) minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static List<BlockOverlayRenderer> getOverlays() {
        return overlays;
    }

    public static void setOverlays(List<BlockOverlayRenderer> overlays) {
        BlockOverlayRenderer.overlays = overlays;
    }
}
