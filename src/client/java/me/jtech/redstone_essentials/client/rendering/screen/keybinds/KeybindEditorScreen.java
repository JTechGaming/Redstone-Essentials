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

import java.util.ArrayList;
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

    private ButtonWidget intervalButton;
    private ButtonWidget copyTextButton;
    private ButtonWidget cycleStateButton;
    private ButtonWidget holdKeyButton;
    private ButtonWidget sendToastButton;

    private ButtonWidget cycleStateRotateButton;

    private TextFieldWidget intervalField;
    private TextFieldWidget toastField;
    private TextFieldWidget copyTextField;
    private TextFieldWidget cycleStateField;

    private boolean isIntervalEnabled = false;
    private boolean isCopyTextEnabled = false;
    private boolean isCycleStateEnabled = false;
    private boolean isHoldKeyEnabled = false;
    private boolean isSendToastEnabled = false;

    private long intervalTime = 0;

    private String copyText = "";
    private String toastText = "";

    private int currentCycleState = 0;
    private List<String> cycleStates = new ArrayList<>();

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

        if (cycleStates.isEmpty()) {
            cycleStates.add("");
        }

        // Initialize with existing values if editing
        if (keybind != null) {
            this.nameField.setText(keybind.getName());
            this.commandField.setText(keybind.getCommand());
            DynamicKeybindProperties properties = DynamicKeybindHandler.getKeybindProperties(keybind.getName());
            if (properties != null) {
                loadProperties(properties);
            }
        }

        DynamicKeybindHandler.setupKeyDetection(this); // Setup key detection

        // Button to input the keys
        this.keyButton = ButtonWidget.builder(Text.literal(keybind == null ? "Key: ..." : "Key: " + keyNameQuery(keyList)), button -> {
            keyButton.active = false;
            DynamicKeybindHandler.isReceiving = true;
        }).dimensions(this.width / 2 - 100, 150, 200, 20).build(); // Set dimensions and build the button

        // Delete button
        if (keybind != null) {// Keybind can't be null (because then you can't delete it ofc)
            this.deleteButton = ButtonWidget.builder(Text.literal("Delete"), button -> {
                DynamicKeybindHandler.removeKeybind(keybind.getName()); // Remove keybind from keybind handler registry
                KeybindRegistry.remove(keybind); // Remove keybind from keybind screen registry
                DynamicKeybindHandler.saveKeybinds(); // Save all keybinds to config file
                MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent)); // Go back to the list
            }).dimensions(this.width / 2 - 50, this.height - 100, 100, 20).build(); // Set dimensions and build the button

            this.addDrawableChild(deleteButton); // This one line took 2 hours to figure out lol
        }

        // Interval button
        this.intervalButton = ButtonWidget.builder(Text.literal("Interval: " + (isIntervalEnabled ? "On" : "Off")), button -> {
            isIntervalEnabled = !isIntervalEnabled;
            intervalButton.setMessage(Text.literal("Interval: " + (isIntervalEnabled ? "On" : "Off")));
        }).dimensions(this.width / 2 + 100, this.height - 125, 100, 20).build(); // Set dimensions and build the button

        this.intervalField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 + 10, this.height - 125, 85, 20, Text.literal(""));
        this.intervalField.setPlaceholder(Text.literal("Interval Time"));

        this.copyTextField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 + 10, this.height - 150, 85, 20, Text.literal(""));
        this.copyTextField.setPlaceholder(Text.literal("Copy Text"));

        this.toastField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 90, this.height - 175, 85, 20, Text.literal(""));
        this.toastField.setPlaceholder(Text.literal("Toast Message"));

        copyTextField.setText(copyText);
        toastField.setText(toastText);
        intervalField.setText(String.valueOf(intervalTime));

        this.cycleStateField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 + 10, this.height - 175, 60, 20, Text.literal(""));

        cycleStateField.setText(cycleStates.get(currentCycleState));

        this.cycleStateRotateButton = ButtonWidget.builder(Text.literal("C"), button -> {
            cycleStates.set(currentCycleState, cycleStateField.getText());
            currentCycleState++;
            if (currentCycleState >= cycleStates.size() && !cycleStates.get(currentCycleState-1).isBlank()) {
                cycleStates.add("");
            }
            if (cycleStates.get(currentCycleState-1).isBlank()) {
                currentCycleState = 0;
            }
            cycleStateField.setText(cycleStates.get(currentCycleState));
        }).dimensions(this.width / 2 + 75, this.height - 175, 20, 20).build(); // Set dimensions and build the button

        // Copy text button
        this.copyTextButton = ButtonWidget.builder(Text.literal("Copy Text: " + (isCopyTextEnabled ? "On" : "Off")), button -> {
            isCopyTextEnabled = !isCopyTextEnabled;
            copyTextButton.setMessage(Text.literal("Copy Text: " + (isCopyTextEnabled ? "On" : "Off")));
        }).dimensions(this.width / 2 + 100, this.height - 150, 100, 20).build(); // Set dimensions and build the button

        // Cycle state button
        this.cycleStateButton = ButtonWidget.builder(Text.literal("Cycle State: " + (isCycleStateEnabled ? "On" : "Off")), button -> {
            isCycleStateEnabled = !isCycleStateEnabled;
            cycleStateButton.setMessage(Text.literal("Cycle State: " + (isCycleStateEnabled ? "On" : "Off")));
        }).dimensions(this.width / 2 + 100, this.height - 175, 100, 20).build(); // Set dimensions and build the button

        // Hold key button
        this.holdKeyButton = ButtonWidget.builder(Text.literal("Hold Key: " + (isHoldKeyEnabled ? "On" : "Off")), button -> {
            isHoldKeyEnabled = !isHoldKeyEnabled;
            holdKeyButton.setMessage(Text.literal("Hold Key: " + (isHoldKeyEnabled ? "On" : "Off")));
        }).dimensions(this.width / 2 - 200, this.height - 150, 100, 20).build(); // Set dimensions and build the button

        // Send toast button
        this.sendToastButton = ButtonWidget.builder(Text.literal("Send Toast: " + (isSendToastEnabled ? "On" : "Off")), button -> {
            isSendToastEnabled = !isSendToastEnabled;
            sendToastButton.setMessage(Text.literal("Send Toast: " + (isSendToastEnabled ? "On" : "Off")));
        }).dimensions(this.width / 2 - 200, this.height - 175, 100, 20).build(); // Set dimensions and build the button

        // Save and Cancel buttons
        this.saveButton = ButtonWidget.builder(Text.literal("Save"), button -> { // Start button widget builder
            DynamicKeybindHandler.endKeyDetection(this); // End key detection
            // Save keybind data here (either update existing or create new)
            if (keybind == null) { // If the keybind is null, create a new one
                KeybindEntry newKeybind = new KeybindEntry(this.nameField.getText(), this.commandField.getText().replace("/", ""), keyList, false, false); // Create a new keybind entry
                DynamicKeybindProperties properties = new DynamicKeybindProperties(); // Create new properties
                properties.command = newKeybind.getCommand(); // Set the command in the new properties variable to the command value
                updateProperties(properties);
                KeybindRegistry.register(newKeybind, properties); // Custom logic to add the keybind
            } else { // Otherwise, we modify it
                int i = KeybindRegistry.getKeybinds().indexOf(keybind); // Get the array index of the keybind in the registry

                DynamicKeybindHandler.removeKeybind(keybind.getName()); // Remove the old version of the keybind from the handler

                keybind.setName(this.nameField.getText()); // Update the name value
                keybind.setCommand(this.commandField.getText().replace("/", "")); // Update the command value
                keybind.setKey(this.keyList); // Update the keys

                DynamicKeybindProperties properties = new DynamicKeybindProperties(); // Create new properties
                properties.command = keybind.getCommand(); // Set the command in the new properties variable to the updated command value

                updateProperties(properties);

                DynamicKeybindHandler.addKeybind(keybind.getName(), keybind.getKey(), properties); // Add the new keybind the keybind handler registry

                KeybindRegistry.getKeybinds().set(i, keybind); // Also add the new keybind to the keybind screen registry
            }
            DynamicKeybindHandler.saveKeybinds(); // Save all keybinds to config file
            MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent)); // Go back to the list
        }).dimensions(this.width / 2 - 50, this.height - 40, 100, 20).build(); // Set dimensions and build the button

        this.cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> {
            DynamicKeybindHandler.endKeyDetection(this); // End key detection
            MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent)); // Go back to the list
        }).dimensions(this.width / 2 - 50, this.height - 70, 100, 20).build(); // Set dimensions and build the button

        // Add widgets
        this.addDrawableChild(nameField);
        this.addDrawableChild(commandField);
        this.addDrawableChild(keyButton);

        this.addDrawableChild(intervalButton);
        this.addDrawableChild(intervalField);
        this.addDrawableChild(copyTextButton);
        this.addDrawableChild(copyTextField);
        this.addDrawableChild(cycleStateButton);
        this.addDrawableChild(cycleStateField);
        this.addDrawableChild(holdKeyButton);
        this.addDrawableChild(sendToastButton);
        this.addDrawableChild(toastField);

        this.addDrawableChild(cycleStateRotateButton);

        this.addDrawableChild(saveButton);
        this.addDrawableChild(cancelButton);
    }

    private void updateProperties(DynamicKeybindProperties properties) {
        intervalTime = Long.parseLong(intervalField.getText());
        toastText = toastField.getText();

        properties.name = nameField.getText();
        properties.hasToggleInterval = isIntervalEnabled;
        properties.interval = intervalTime;
        properties.copyText = isCopyTextEnabled;
        properties.copyTextMessage = copyTextField.getText();
        properties.hasCycleState = isCycleStateEnabled;
        properties.cycleStates = cycleStates;
        properties.hasHoldKey = isHoldKeyEnabled;
        properties.hasSendToast = isSendToastEnabled;
        properties.toastMessage = toastText;
    }

    private void loadProperties(DynamicKeybindProperties properties) {
        isIntervalEnabled = properties.hasToggleInterval;
        intervalTime = properties.interval;
        isCopyTextEnabled = properties.copyText;
        copyText = properties.copyTextMessage;
        isCycleStateEnabled = properties.hasCycleState;
        cycleStates = properties.cycleStates;
        isHoldKeyEnabled = properties.hasHoldKey;
        isSendToastEnabled = properties.hasSendToast;
        toastText = properties.toastMessage;
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
        this.keyButton.active = true;
        Redstone_Essentials.shouldApplyButtonStyle = false;
        this.client.setScreen(null);
    }

    public void resetInputKey() {
        this.keyButton.active = true;
    }
}

