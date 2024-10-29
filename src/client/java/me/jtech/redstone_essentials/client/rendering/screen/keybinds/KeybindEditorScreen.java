package me.jtech.redstone_essentials.client.rendering.screen.keybinds;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.client.keybinds.DynamicKeybindHandler;
import me.jtech.redstone_essentials.client.keybinds.DynamicKeybindProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

//public class KeybindEditorScreen extends HandledScreen<ScreenHandler> {
public class KeybindEditorScreen extends Screen {

    private final KeybindEntry keybind; // Existing keybind to edit, or null for a new one
    private TextFieldWidget nameField;
    private TextFieldWidget commandField;
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;
    private ButtonWidget deleteButton;
    private ButtonWidget keyButton;

    private boolean shiftRequired;
    private boolean ctrlRequired;

    private List<Integer> keyList;

    public KeybindEditorScreen(KeybindEntry keybind, List<Integer> keys) {
        super(Text.literal(keybind == null ? "Create Keybind" : "Edit Keybind"));
        this.keybind = keybind;
        this.keyList = keys;
    }

    @Override
    protected void init() {
        super.init();
        this.nameField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, 50, 200, 20, Text.literal("Keybind Name"));
        this.commandField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, 100, 200, 20, Text.literal("Command"));

        // Initialize with existing values if editing
        if (keybind != null) {
            this.nameField.setText(keybind.getName());
            this.commandField.setText(keybind.getCommand());
        }

        // Button to input the keys
        this.keyButton = ButtonWidget.builder(Text.literal(keybind == null ? "Key: ..." : "Key: " + keyNameQuery(keyList)), button -> {
            if (!DynamicKeybindHandler.isWaitingForKey) {
                this.keyButton.active = false;
                this.keyButton.setFocused(false);
                DynamicKeybindHandler.waitForKeyInput(this); // Tell the DynamicKeybindHandler to start registring key inputs
            }
        }).dimensions(this.width / 2 - 100, 150, 200, 20).build(); // Set dimensions and build the button

        // Delete button
        if (keybind != null) {// Keybind can't be null (because then you can't delete it ofc)
            this.deleteButton = ButtonWidget.builder(Text.literal("Delete"), button -> {
                DynamicKeybindHandler.isWaitingForKey = false;
                DynamicKeybindHandler.removeKeybind(keybind.getName()); // Remove keybind from keybind handler registry
                KeybindRegistry.remove(keybind); // Remove keybind from keybind screen registry
                DynamicKeybindHandler.saveKeybinds(); // Save all keybinds to config file
                MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent)); // Go back to the list
            }).dimensions(this.width / 2 - 50, this.height - 100, 100, 20).build(); // Set dimensions and build the button

            this.addDrawableChild(deleteButton); // This one line took 2 hours to figure out lol
        }

        // Save and Cancel buttons
        this.saveButton = ButtonWidget.builder(Text.literal("Save"), button -> { // Start button widget builder
            DynamicKeybindHandler.isWaitingForKey = false;
            // Save keybind data here (either update existing or create new)
            if (keybind == null) { // If the keybind is null, create a new one
                KeybindEntry newKeybind = new KeybindEntry(this.nameField.getText(), this.commandField.getText(), keyList, shiftRequired, ctrlRequired); // Create a new keybind entry
                KeybindRegistry.register(newKeybind); // Custom logic to add the keybind
            } else { // Otherwise, we modify it
                int i = KeybindRegistry.getKeybinds().indexOf(keybind); // Get the array index of the keybind in the registry

                DynamicKeybindHandler.removeKeybind(keybind.getName()); // Remove the old version of the keybind from the handler

                keybind.setName(this.nameField.getText()); // Update the name value
                keybind.setCommand(this.commandField.getText()); // Update the command value
                keybind.setKey(this.keyList); // Update the keys
                keybind.setShiftRequired(shiftRequired); // not in use
                keybind.setCtrlRequired(ctrlRequired); // not in use

                DynamicKeybindProperties properties = new DynamicKeybindProperties(); // Create new properties
                properties.command = keybind.getCommand(); // Set the command in the new properties variable to the updated command value
                DynamicKeybindHandler.addKeybind(keybind.getName(), keybind.getKey(), properties); // Add the new keybind the the keybind handler registry

                KeybindRegistry.getKeybinds().set(i, keybind); // Also add the new keybind to the keybind screen registry
            }
            DynamicKeybindHandler.saveKeybinds(); // Save all keybinds to config file
            MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent)); // Go back to the list
        }).dimensions(this.width / 2 - 50, this.height - 40, 100, 20).build(); // Set dimensions and build the button

        this.cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> {
            DynamicKeybindHandler.isWaitingForKey = false;
            MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent)); // Go back to the list
        }).dimensions(this.width / 2 - 50, this.height - 70, 100, 20).build(); // Set dimensions and build the button

        // Add widgets
        this.addDrawableChild(nameField);
        this.addDrawableChild(commandField);
        this.addDrawableChild(keyButton);
        this.addDrawableChild(saveButton);
        this.addDrawableChild(cancelButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) { //TODO comment this
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.nameField.render(context, mouseX, mouseY, delta);
        this.commandField.render(context, mouseX, mouseY, delta);
    }

    public void setKeys(List<Integer> keys) { //TODO comment this
        keyList = keys;
        String temp = "";
        for (int i : keys) {
            temp = temp.concat(" , " + i);
        }
        String temp2 = "";
        for (int i : keyList) {
            temp2 = temp2.concat(" , " + i);
        }
        keyButton.setMessage(Text.literal("Key: " + keyNameQuery(keyList)));
    }

    public String keyNameQuery(List<Integer> keys) { //TODO comment this
        String out = "";
        for (int i=0; i<keys.size(); i++) {
            out = out.concat(GLFW.glfwGetKeyName(keys.get(i), 0) + (i<keys.size()-1? " + " : ""));
        }
        return out;
    }

    @Override
    public void close() { //TODO comment this
        DynamicKeybindHandler.isWaitingForKey = false;
        this.keyButton.active = true;
        Redstone_Essentials.shouldApplyButtonStyle = false;
        this.client.setScreen(null);
    }

    public void resetInputKey() {
        DynamicKeybindHandler.isWaitingForKey = false;
        this.keyButton.active = true;
    }
}

