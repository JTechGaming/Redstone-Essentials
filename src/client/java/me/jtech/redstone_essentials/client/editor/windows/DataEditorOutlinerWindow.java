package me.jtech.redstone_essentials.client.editor.windows;

import imgui.ImGui;
import imgui.flag.ImGuiDragDropFlags;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataEditorOutlinerWindow {
    private static Map<String, ItemStack> items = new HashMap<>();

    public static void render() {
        ImGui.begin("DataEditorOutliner", ImGuiWindowFlags.AlwaysAutoResize);
        if (ImGui.beginDragDropTarget()) {
            ItemStack payload = ImGui.acceptDragDropPayload("DND_FULL_ITEM");
            if (payload != null) {
                items.put("General", payload);
            }
            ImGui.endDragDropTarget();
        }
        for (ItemStack item : items.values()) { // TODO make a category system
            ImGui.text(item.getName().getString());
            if (ImGui.beginDragDropSource(ImGuiDragDropFlags.None)) {
                ImGui.setDragDropPayload("DND_ITEM_FROM_OUTLINER", item);
                ImGui.text(item.toString());
                ImGui.endDragDropSource();
            }
//            ImGui.sameLine();
//            if (ImGui.button("Copy")) {
//                StringSelection stringSelection = new StringSelection(item.getName().getString());
//                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                clipboard.setContents(stringSelection, null);
//            }
            ImGui.sameLine();
            if (ImGui.button("Delete")) {
                items.remove(item);
            }
        }
        ImGui.end();
    }
}
