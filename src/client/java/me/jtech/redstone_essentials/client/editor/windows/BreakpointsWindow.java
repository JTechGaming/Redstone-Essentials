package me.jtech.redstone_essentials.client.editor.windows;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.client.debugger.ClientDebugSession;
import me.jtech.redstone_essentials.debugger.DebugManager;
import me.jtech.redstone_essentials.debugger.DebuggerSession;
import me.jtech.redstone_essentials.utility.BreakpointCondition;
import net.minecraft.state.property.Property;

import java.util.Map;

public class BreakpointsWindow {

    public static void render() {
        ClientDebugSession session = Redstone_Essentials_Client.getInstance().debugSession;
        ImGui.begin("Breakpoints", ImGuiWindowFlags.AlwaysAutoResize);

        ImGui.separator();

        BreakpointCondition selectedBlockBreakpoint = null;
        Long selectedTickBreakpoint = null;

        if (session == null) {
            ImGui.text("No Debugger Session Active");
            ImGui.end();
            return;
        }

        // Display Block Breakpoints
        for (BreakpointCondition bp : session.getBlockBreakpoints()) {
            boolean isEnabled = session.isBlockBreakpointEnabled(bp);
            String label = (isEnabled ? "📌 " : "🚫 ") + bp.getBlock().getName().getString() +
                    " (" + bp.getPosition().getX() + ", " + bp.getPosition().getY() + ", " + bp.getPosition().getZ() + ")";

            if (!bp.conditions.isEmpty()) {
                label += " [";
                for (Map.Entry<Property<?>, String> entry : bp.conditions.entrySet()) {
                    label += entry.getKey().getName() + ":" + entry.getValue() + " ";
                }
                label = label.trim() + "]";
            }

            if (ImGui.selectable(label)) {
                session.jumpToBlock(bp.getPosition());
            }

            if (ImGui.isItemHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                selectedBlockBreakpoint = bp;
            }
        }

        // Display Tick Breakpoints
        for (Long tick : session.getTickBreakpoints()) {
            boolean isEnabled = session.isTickBreakpointEnabled(tick);
            String label = (isEnabled ? "🕒 " : "⏸ ") + "Tick " + tick;

            if (ImGui.selectable(label)) {
                session.setCurrentTick(tick);
            }

            if (ImGui.isItemHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                selectedTickBreakpoint = tick;
            }
        }

        ImGui.separator();

        // Toggle Breakpoint
        if (selectedBlockBreakpoint != null && ImGui.button("Toggle Block Breakpoint")) {
            session.toggleBlockBreakpoint(selectedBlockBreakpoint);
        }

        if (selectedTickBreakpoint != null && ImGui.button("Toggle Tick Breakpoint")) {
            session.toggleTickBreakpoint(selectedTickBreakpoint);
        }

        // Remove Breakpoints
        if (selectedBlockBreakpoint != null && ImGui.button("Remove Block Breakpoint")) {
            session.removeBlockBreakpoint(selectedBlockBreakpoint);
        }

        if (selectedTickBreakpoint != null && ImGui.button("Remove Tick Breakpoint")) {
            session.removeTickBreakpoint(selectedTickBreakpoint);
        }

        ImGui.end();
    }
}
