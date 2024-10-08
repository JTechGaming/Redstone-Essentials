package me.jtech.redstonecomptools.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

//public class KeybindEditorScreen extends HandledScreen<ScreenHandler> {
public class KeybindEditorScreen extends Screen {

    private final KeybindEntry keybind; // Existing keybind to edit, or null for a new one
    private TextFieldWidget nameField;
    private TextFieldWidget commandField;
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;

    private boolean shiftRequired;
    private boolean ctrlRequired;

    public KeybindEditorScreen(KeybindEntry keybind) {
        //super(MinecraftClient.getInstance().player.getInventory(), Text.literal(keybind == null ? "Create Keybind" : "Edit Keybind"));
        super(Text.literal(keybind == null ? "Create Keybind" : "Edit Keybind"));
        this.keybind = keybind;
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

        // Save and Cancel buttons
        this.saveButton = new PublicButtonWidget(this.width / 2 - 50, this.height - 40, 100, 20, Text.literal("Save"), button -> {
            // Save keybind data here (either update existing or create new)
            if (keybind == null) {
                KeybindEntry newKeybind = new KeybindEntry(this.nameField.getText(), this.commandField.getText(), shiftRequired, ctrlRequired);
                KeybindRegistry.register(newKeybind); // Custom logic to add the keybind
            } else {
                keybind.setName(this.nameField.getText());
                keybind.setCommand(this.commandField.getText());
                keybind.setShiftRequired(shiftRequired);
                keybind.setCtrlRequired(ctrlRequired);
            }
            MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent)); // Go back to the list
        }, null);

        this.cancelButton = new PublicButtonWidget(this.width / 2 - 50, this.height - 70, 100, 20, Text.literal("Cancel"), button -> {
            MinecraftClient.getInstance().setScreen(new KeybindScreen(KeybindScreen.parent));
        }, null);

        // Add widgets
        this.addDrawableChild(nameField);
        this.addDrawableChild(commandField);
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
}

