package me.jtech.redstone_essentials.client.editor.windows;

import com.mojang.blaze3d.platform.GlStateManager;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.client.debugger.ClientDebugSession;
import me.jtech.redstone_essentials.debugger.DebugManager;
import me.jtech.redstone_essentials.debugger.DebuggerSession;
import me.jtech.redstone_essentials.client.editor.imgui.ImGuiImplementation;
import me.jtech.redstone_essentials.utility.BlockChange;
import me.jtech.redstone_essentials.utility.BreakpointCondition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Property;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;

public class OutlinerWindow {
    public static void render() {
        ClientDebugSession session = Redstone_Essentials_Client.getInstance().debugSession;
        ImGui.begin("Outliner", ImGuiWindowFlags.AlwaysAutoResize);

        if (session == null) {
            ImGui.text("No session active");
            ImGui.end();
            return;
        }

        ImBoolean showAllTicks = new ImBoolean(DebuggerSession.isShowAllTicks());
        ImGui.checkbox("Show All Ticks", showAllTicks);
        DebuggerSession.setShowAllTicks(showAllTicks.get());

        ImGui.separator();

        List<BlockChange> blockChanges = session.getBlockChanges(session.isShowAllTicks());

        BlockChange hoveredBlock = null;

        for (BlockChange change : blockChanges) {
            String label = getBlockFromChange(change).getName().getString() + " (" + change.getPosition().getX() + ", " +
                    change.getPosition().getY() + ", " + change.getPosition().getZ() + ")";

            if (session.isShowAllTicks()) {
                label += " [Tick " + change.getTickNumber() + "]";
            }

            if (ImGui.selectable(label)) {
                session.selectBlock(change);
            }

            if (ImGui.isItemHovered()) {
                hoveredBlock = change;
                renderStateComparisonTooltip(change);
            }

            if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(0)) {
                session.teleportTo(change);
                session.highlightBlock(change);
            }

            if (ImGui.beginPopupContextItem()) {
                if (ImGui.menuItem("Teleport")) {
                    session.teleportTo(change);
                }
                if (ImGui.menuItem("Highlight Block")) {
                    session.highlightBlock(change);
                }
                if (ImGui.menuItem("Set Breakpoint")) {
                    BreakpointCondition condition = new BreakpointCondition(change.getPosition(), getBlockFromChange(change));
                    session.addBlockBreakpoint(condition);
                }
                ImGui.separator();
                if (DebuggerSession.isShowAllTicks() && ImGui.menuItem("Revert to Tick " + change.getTickNumber())) {
                    session.revertToTick(change.getTickNumber());
                }
                if (ImGui.menuItem("Undo Block Change")) {
                    session.undoBlockChange(change);
                }
                ImGui.endPopup();
            }
        }

        ImGui.separator();

        // Render block preview
        if (hoveredBlock != null) {
            ImGui.text("Block Preview:");
            renderBlockPreview(hoveredBlock);
        }

        ImGui.end();
    }

    public static void renderStateComparisonTooltip(BlockChange change) {
        BlockState oldState = change.getOldState();
        BlockState newState = change.getNewState();

        if (oldState == null || newState == null || oldState == newState) return;

        ImGui.beginTooltip();
        ImGui.textColored(1.0f, 1.0f, 0.0f, 1.0f, "Block State Changes:");

        for (Property<?> property : oldState.getProperties()) {
            String oldValue = oldState.get(property).toString();
            String newValue = newState.get(property).toString();

            if (!oldValue.equals(newValue)) {
                ImGui.text(property.getName() + ": ");
                ImGui.sameLine();
                ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, oldValue);
                ImGui.sameLine();
                ImGui.text(" → ");
                ImGui.sameLine();
                ImGui.textColored(0.0f, 1.0f, 0.0f, 1.0f, newValue);
            }
        }

//        if (change.getOldNbt() != null && change.getNewNbt() != null) {
//            renderNBTComparison(change.getOldNbt(), change.getNewNbt());
//        }

        ImGui.endTooltip();
    }

    public static void renderNBTComparison(NbtCompound oldNbt, NbtCompound newNbt) {
        ImGui.textColored(1.0f, 1.0f, 0.0f, 1.0f, "NBT Changes:");

        for (String key : oldNbt.getKeys()) {
            String oldValue = oldNbt.getString(key);
            String newValue = newNbt.getString(key);

            if (!oldValue.equals(newValue)) {
                ImGui.text(key + ": ");
                ImGui.sameLine();
                ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, oldValue);
                ImGui.sameLine();
                ImGui.text(" → ");
                ImGui.sameLine();
                ImGui.textColored(0.0f, 1.0f, 0.0f, 1.0f, newValue);
            }
        }
    }

    public static void renderBlockPreview(BlockChange change) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();

        // Convert BlockState to ItemStack for rendering
        ItemStack stack = new ItemStack(getBlockFromChange(change).asItem());

        if (!stack.isEmpty()) {
            // Render ItemStack to BufferedImage
            BufferedImage image = renderStackToImage(stack, itemRenderer);

            // Convert BufferedImage to OpenGL Texture
            int textureID = ImGuiImplementation.fromBufferedImage(image);

            // Display the texture in ImGui
            ImGui.image(textureID, 64, 64);
        } else {
            ImGui.text("No preview available");
        }
    }

    private static Block getBlockFromChange(BlockChange change) {
        World world = MinecraftClient.getInstance().world;
        return world.getBlockState(change.getPosition()).getBlock();
    }

    private static BufferedImage renderStackToImage(ItemStack stack, ItemRenderer renderer) {
        int width = 64, height = 64;

        // Create a framebuffer to render into
        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
        framebuffer.beginWrite(true);

        // Render the item
        MatrixStack matrices = new MatrixStack();
        matrices.scale(2.0f, 2.0f, 2.0f); // Scale up for better resolution
        //renderer.renderGuiItemIcon(stack, width / 4, height / 4);

        // Read pixels into a BufferedImage
        ByteBuffer buffer = ByteBuffer.allocate(4 * width * height);
        GlStateManager._readPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (height - y - 1) * width + x; // Flip vertically
                image.setRGB(x, y, buffer.get(index));
            }
        }

        framebuffer.endWrite();
        framebuffer.delete(); // Cleanup

        return image;
    }
}
