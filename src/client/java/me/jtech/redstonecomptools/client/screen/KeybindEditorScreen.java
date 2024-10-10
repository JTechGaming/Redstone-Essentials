package me.jtech.redstonecomptools.client.screen;

import me.jtech.redstonecomptools.client.keybinds.DynamicKeybindHandler;
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
    private ButtonWidget keyButton;

    private boolean shiftRequired;
    private boolean ctrlRequired;

    private List<Integer> keyList;

    public KeybindEditorScreen(KeybindEntry keybind, List<Integer> keys) {
        //super(MinecraftClient.getInstance().player.getInventory(), Text.literal(keybind == null ? "Create Keybind" : "Edit Keybind"));
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

        this.keyButton = ButtonWidget.builder(Text.literal(keybind == null ? "Key: ..." : "Key: " + keyNameQuery(keyList)), button -> {
            DynamicKeybindHandler.waitForKeyInput(this);
        }).dimensions(this.width / 2 - 50, 150, 200, 20).build();

        // Save and Cancel buttons
        this.saveButton = ButtonWidget.builder(Text.literal("Save"), button -> {
            // Save keybind data here (either update existing or create new)
            if (keybind == null) {
                KeybindEntry newKeybind = new KeybindEntry(this.nameField.getText(), this.commandField.getText(), keyList, shiftRequired, ctrlRequired);
                KeybindRegistry.register(newKeybind); // Custom logic to add the keybind
            } else {
                keybind.setName(this.nameField.getText());
                keybind.setCommand(this.commandField.getText());
                keybind.setShiftRequired(shiftRequired);
                keybind.setCtrlRequired(ctrlRequired);
            }
            MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent)); // Go back to the list
        }).dimensions(this.width / 2 - 50, this.height - 40, 100, 20).build();

        this.cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> {
            MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent));
        }).dimensions(this.width / 2 - 50, this.height - 70, 100, 20).build();

        // Add widgets
        this.addDrawableChild(nameField);
        this.addDrawableChild(commandField);
        this.addDrawableChild(keyButton);
        this.addDrawableChild(saveButton);
        this.addDrawableChild(cancelButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.nameField.render(context, mouseX, mouseY, delta);
        this.commandField.render(context, mouseX, mouseY, delta);
    }

    public void setKeys(List<Integer> keys) {
        keyList = keys;
        keyButton.setMessage(Text.literal("Key: " + keyNameQuery(keyList)));
    }

    public String keyNameQuery(List<Integer> keys) {
        String out = "";
        for (int i=0; i<keys.size(); i++) {
            out = out.concat(GLFW.glfwGetKeyName(keys.get(i), 0) + (i<keys.size()-1? " + " : ""));
        }
        // TODO remove this sout
        System.out.println(out);
        return out;
    }
}

