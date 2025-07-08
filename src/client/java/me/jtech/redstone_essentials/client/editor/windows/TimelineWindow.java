package me.jtech.redstone_essentials.client.editor.windows;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import me.jtech.redstone_essentials.client.IO.FileDialog;
import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.client.debugger.ClientDebugSession;
import me.jtech.redstone_essentials.debugger.DebugManager;
import me.jtech.redstone_essentials.debugger.DebuggerSession;
import me.jtech.redstone_essentials.utility.TileTickEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TimelineWindow {
    // Timeline Zoom & Pan Controls
    static float zoomLevel = 1.0f; // Default zoom (1.0 = full range)
    static float panOffset = 0.0f; // Offset for panning
    static boolean isPanning = false;

    public static void render() {
        ClientDebugSession session = Redstone_Essentials_Client.getInstance().debugSession;
        ImGui.begin("Redstone Debugger"); // Window title

        if (session == null) {
            ImGui.text("No session active");
//            if (ImGui.button("Start Debugging Session")) {
//                //DebugManager.startDebuggerSession();
//            }
            //ImGui.end();
            return;
        }

        // Step Controls
        if (ImGui.button("Pause/Resume")) {
            session.togglePause();
        }
        ImGui.sameLine();
        if (ImGui.button("Step Tick")) {
            session.stepForwardTick();
        }
        if (ImGui.button("Step Tick Backward")) {
            session.stepBackwardTick();
        }
        ImGui.sameLine();
        if (ImGui.button("Step TileTick")) {
            //session.stepForwardTileTick(MinecraftClient.getInstance().player.getWorld());
        }

        ImGui.text("Replay Controls:");

        if (ImGui.button("Play")) {
            session.startReplay();
        }
        ImGui.sameLine();
        if (ImGui.button("Pause")) {
            session.pauseReplay();
        }
        ImGui.sameLine();
        if (ImGui.button("Fast Forward")) {
            session.setReplaySpeed(2.0);
        }
        ImGui.sameLine();
        if (ImGui.button("Rewind")) {
            session.rewindReplay();
        }

        ImGui.separator();

// Handle Zooming
        if (ImGui.isWindowHovered() && ImGui.getIO().getMouseWheel() != 0) {
            float zoomChange = ImGui.getIO().getMouseWheel() * 0.1f;
            zoomLevel = Math.max(0.1f, Math.min(5.0f, zoomLevel + zoomChange)); // Clamp zoom between 0.1x and 5x
        }

// Handle Panning (Right Mouse Drag)
        if (ImGui.isWindowHovered() && ImGui.isMouseDragging(ImGuiMouseButton.Right)) {
            isPanning = true;
            panOffset -= ImGui.getIO().getMouseDeltaX(); // Adjust pan based on mouse movement
        } else {
            isPanning = false;
        }

        ImGui.text("Tick Timeline:");
        ImGui.beginChild("##timeline", ImGui.getContentRegionAvailX(), 80, true, ImGuiWindowFlags.HorizontalScrollbar);

        long minTick = session.getMinTick();
        long maxTick = session.getMaxTick();
        long tickRange = (long) ((maxTick - minTick + 1) / zoomLevel);
        long startTick = Math.max(minTick, minTick + (long) (panOffset / 10)); // Convert panOffset to ticks
        long endTick = Math.min(maxTick, startTick + tickRange);

// Prevent Panning Out of Bounds
        if (startTick <= minTick) panOffset = 0;
        if (endTick >= maxTick) panOffset = (maxTick - minTick) * 10;

// Draw the timeline ticks
        for (long tick = startTick; tick <= endTick; tick++) {
            if (tick % 10 == 0) ImGui.separator(); // Tick markers
            if (ImGui.button(String.valueOf(tick))) {
                session.setCurrentTick(tick);
            }
            ImGui.sameLine();
        }

// Draw the playhead
        float timelineWidth = ImGui.getContentRegionAvailX();
        float cursorPos = (float) (session.getCurrentTick() - startTick) / (endTick - startTick) * timelineWidth;

        ImGui.setCursorPosX(cursorPos);
        ImGui.textColored(1.0f, 1.0f, 0.0f, 1.0f, "|"); // Playhead marker

// Handle dragging the playhead
        ImGui.setCursorPosX(cursorPos - 5);
        ImGui.invisibleButton("##playhead", 10, 20);
        if (ImGui.isItemActive()) {
            float mouseX = ImGui.getMousePosX() - ImGui.getCursorScreenPosX();
            long newTick = (long) ((mouseX / timelineWidth) * (endTick - startTick)) + startTick;
            session.setCurrentTick(newTick);
        }

        ImGui.endChild();

        ImGui.separator();

        // Tile Tick Execution Order
        ImGui.text("Scheduled Tile Ticks:");
        ImGui.beginChild("##tileticks", ImGui.getContentRegionAvailX(), 200, true);

        List<TileTickEntry> scheduledTicks = session.getTileTicksForCurrentTick(); // TODO ????
        for (TileTickEntry entry : scheduledTicks) {
            if (ImGui.selectable(entry.getBlock().getName().getString() + " at " + entry.getPosition())) {
                //session.selectBlock(entry); // TODO ????
            }
            if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
                session.addTickBreakpoint(entry.getDelay()); // TODO ????
            }
        }

        ImGui.endChild();

        ImGui.separator();

        // Block Change Details
        if (session.hasSelectedBlock()) {
            ImGui.text("Selected Block: " + session.getSelectedBlock().getNewState().getBlock().getName().getString());
            ImGui.text("Position: " + session.getSelectedBlock().getPosition());
        }

        if (ImGui.button("Export Session")) {
            session.exportSession();
        }

        if (ImGui.button("Import Session")) {
            String defaultFolder = FabricLoader.getInstance().getConfigDir().resolve("redstone_essentials").toString();
            FileDialog.openFileDialog(defaultFolder, "Debugging Session", "json").thenAccept(path -> session.importSession(Path.of(path)));
        }

        ImGui.end();
    }
}
