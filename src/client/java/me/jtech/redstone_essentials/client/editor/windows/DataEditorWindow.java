package me.jtech.redstone_essentials.client.editor.windows;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.*;
import imgui.type.ImInt;
import imgui.type.ImString;
import me.jtech.redstone_essentials.Redstone_Essentials;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataEditorWindow {

    private static ImString target = new ImString(128);
    private static ImString item = new ImString(128);
    private static ImString command = new ImString(512);
    private static ImInt count = new ImInt(1);
    private static String selectedCommand = "";
    private static Map<ComponentType<?>, ImString> components = new HashMap<>();

    public static ItemStack selectedItem = null;

    public static void selectItem(ItemStack itemStack) {
        selectedItem = itemStack;
        components.clear();
        for (ComponentType<?> component : itemStack.getComponents().getTypes()) {
            ImString componentData = new ImString(128);
            NbtElement nbtElement = getFromComponent(itemStack, component, MinecraftClient.getInstance().world.getRegistryManager());
            if (nbtElement != null) {
                componentData.set(nbtElement.asString());
            }
            components.put(component, componentData);
        }
    }

    public static void render() {
        //ImGui.showDemoWindow();
        ImGui.begin("DataEditor", ImGuiWindowFlags.AlwaysAutoResize);

        if (ImGui.beginTabBar("DataEditorTabs", ImGuiTabBarFlags.Reorderable | ImGuiTabBarFlags.AutoSelectNewTabs | ImGuiTabBarFlags.TabListPopupButton)) {
            if (ImGui.beginTabItem("Item Editor", ImGuiTabItemFlags.None)) {
                // Item Editor
                renderItemEditor();
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Command Editor", ImGuiTabItemFlags.None)) {
                renderCommandEditor();
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }

        ImGui.end();
    }

    public static void renderItemEditor() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            if (selectedItem == null) {
                ImGui.text("No Item Selected");
                if (ImGui.button("Select Hand Item")) {
                    selectItem(player.getMainHandStack());
                }
                return;
            }

            ImVec2 itemRectSize = new ImVec2(ImGui.getWindowSize());
            ImGui.dummy(itemRectSize.x, itemRectSize.y);
            if (ImGui.beginDragDropTarget()) {
                ItemStack payload = ImGui.acceptDragDropPayload("DND_ITEM_FROM_OUTLINER");
                if (payload != null) {
                    selectItem(payload);
                }

                Identifier payloadId = ImGui.acceptDragDropPayload("DND_FILE");
                if (payloadId != null) {
                    ComponentType<?> componentType = parse(payloadId);
                    if (componentType != null) {
                        ImString componentData = new ImString(128);
                        components.put(componentType, componentData);
                    }
                }

                ImGui.endDragDropTarget();
            }

            Item item = selectedItem.getItem();

            // Put the imgui cursor back to the top left
            ImGui.setCursorPos(ImGui.getCursorPosX() - itemRectSize.x, ImGui.getCursorPosY() - itemRectSize.y);

            if (item != Items.AIR) {
                // Render the item
                ImGui.text("Item: " + item.getName(selectedItem).getString());
//                if (selectedItem != null) {
//                    if (ImGui.beginDragDropSource(ImGuiDragDropFlags.None)) {
//                        ImGui.setDragDropPayload("DND_FULL_ITEM", selectedItem);
//                        ImGui.text(selectedItem.getName().getString());
//                        ImGui.endDragDropSource();
//                    }
//                }
            } else {
                ImGui.text("No item selected");
                return;
            }

            // Render the item components
            ImGui.text("Components:");
            for (ComponentType<?> component : components.keySet()) {
                ImString selectedComponent = components.get(component);
                if (ImGui.collapsingHeader(component.toString())) {
                    // Render the component data
                    ImGui.inputText("Data", selectedComponent);
                    try {
                        String nbtInput = selectedComponent.get();
                        NbtElement nbtElement = StringNbtReader.parse(nbtInput); // Parse to NbtElement

                        // Suppress unchecked warning due to generic cast
                        @SuppressWarnings("unchecked")
                        ComponentType<Object> objComponent = (ComponentType<Object>) component;
                        DynamicRegistryManager registryManager = MinecraftClient.getInstance().world.getRegistryManager();
                        setFromNbt(selectedItem, objComponent, nbtElement, registryManager); // Apply to stack
                    } catch (CommandSyntaxException e) {
                        //Redstone_Essentials.LOGGER.warn("Invalid NBT format: {}", e.getMessage());
                        // Do nothing
                    }
                }
                if (ImGui.beginDragDropSource(ImGuiDragDropFlags.None)) {
                    ImGui.setDragDropPayload("DND_RETURN_COMPONENT", component);
                    ImGui.text(component.toString());
                    ImGui.endDragDropSource();
                }
            }
            renderComponentsDrawer();
        }
    }

    public static void renderCommandEditor() {
        // Basically an mc stacker clone
        // Command Selector
        ImGui.text("Command:");
        ImGui.sameLine();
        if (ImGui.beginCombo("##commandSelector", "Select Command")) {
            ImGui.selectable("give");
            if (ImGui.isItemClicked()) {
                selectedCommand = "give";
            }
            ImGui.selectable("summon");
            if (ImGui.isItemClicked()) {
                selectedCommand = "summon";
            }
            ImGui.selectable("setblock");
            if (ImGui.isItemClicked()) {
                selectedCommand = "setblock";
            }
            ImGui.selectable("tp");
            if (ImGui.isItemClicked()) {
                selectedCommand = "tp";
            }
            ImGui.endCombo();
        }

        ImGui.separator();

        // Parameter Inputs
        ImGui.text("Parameters:");
        ImGui.inputText("Target", target, ImGuiInputTextFlags.None);
        ImGui.inputText("Item/Block", item, ImGuiInputTextFlags.None);
        ImGui.inputInt("Count", count);

        ImGui.separator();

        // Output Command
        ImGui.text("Generated Command:");
        command.set("/" + selectedCommand + " " + target.get() + " " + item.get() + " " + count.get());
        ImGui.inputTextMultiline("##outputCommand", command, ImGuiInputTextFlags.ReadOnly);

        ImGui.separator();

        // Copy Button
        if (ImGui.button("Copy to Clipboard")) {
            // Logic to copy the generated command to clipboard
            StringSelection selection = new StringSelection(command.get());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        }
    }

    public static <T> NbtElement getFromComponent(ItemStack stack, ComponentType<T> component, DynamicRegistryManager registryManager) {
        T value = stack.get(component);
        if (value == null) {
            Redstone_Essentials.LOGGER.warn("Component not found on ItemStack: {}", component);
            return NbtOps.INSTANCE.empty();
        }

        DataResult<NbtElement> element = component.getCodecOrThrow().encodeStart(
                registryManager.getOps(NbtOps.INSTANCE),
                value
        );

        return element.getOrThrow();
    }

    public static <T> void setFromNbt(ItemStack stack, ComponentType<T> component, NbtElement element, DynamicRegistryManager registryManager) {
        DataResult<T> value = component.getCodecOrThrow().parse(registryManager.getOps(NbtOps.INSTANCE), element);
        stack.set(component, value.getOrThrow());
    }

    private static void renderComponentsDrawer() {
        // Renders a list of item components that can be dragged onto the item editor
        ImGui.begin("Components", ImGuiWindowFlags.None);
        if (ImGui.beginDragDropTarget()) {
            ComponentType<?> payload = ImGui.acceptDragDropPayload("DND_RETURN_COMPONENT");
            if (payload != null) {
                components.remove(payload);
            }
            ImGui.endDragDropTarget();
        }
        DynamicRegistryManager registryManager = MinecraftClient.getInstance().world.getRegistryManager();
        registryManager.get(RegistryKeys.DATA_COMPONENT_TYPE).getIds().forEach((key) -> {
            if (components.containsKey(registryManager.get(RegistryKeys.DATA_COMPONENT_TYPE).get(key))) {
                return;
            }
            ImGui.selectable(key.toString());
            if (ImGui.isItemHovered()) {
                // Show tooltip with component info
                ImGui.setTooltip("Component: " + key);
            }
            if (ImGui.beginDragDropSource(ImGuiDragDropFlags.None)) {
                ImGui.setDragDropPayload("DND_FILE", key);
                ImGui.text(key.toString());
                ImGui.endDragDropSource();
            }
        });
        ImGui.end();
    }

    private static ComponentType<?> parse(Identifier identifier) {
        DynamicRegistryManager registryManager = MinecraftClient.getInstance().world.getRegistryManager();
        //TODO check if the component type is valid
        return registryManager.get(RegistryKeys.DATA_COMPONENT_TYPE).get(identifier);
    }
}
