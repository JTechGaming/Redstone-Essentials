package me.jtech.redstonecomptools.client.rendering.screen.BitmapPrinter;

import me.jtech.redstonecomptools.SelectionData;
import me.jtech.redstonecomptools.Redstonecomptools;
import me.jtech.redstonecomptools.client.RedstonecomptoolsClient;
import me.jtech.redstonecomptools.client.clientAbilities.SelectionAbility;
import me.jtech.redstonecomptools.client.rendering.BlockOverlayRenderer;
import me.jtech.redstonecomptools.client.rendering.screen.widgets.PlaceholderTextFieldWidget;
import me.jtech.redstonecomptools.utility.IClientSelectionContext;
import me.jtech.redstonecomptools.client.utility.Toaster;
import me.jtech.redstonecomptools.commands.BitmapPrinterCommand;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BitmapPrinterScreen extends Screen implements IClientSelectionContext {
    private static boolean shouldRender;
    private static boolean isRendererSetup = false;
    private static String currentGuiText;
    private PlaceholderTextFieldWidget nameField;
    private ButtonWidget startSelect;
    private PlaceholderTextFieldWidget filePathField;
    private PlaceholderTextFieldWidget screenWidthField;
    private PlaceholderTextFieldWidget screenHeightField;
    private PlaceholderTextFieldWidget intervalField;
    private PlaceholderTextFieldWidget channelsField;
    private ButtonWidget startPrint;

    private static String currentName = "";
    private static String currentPath = "";
    private static String currentScreenWidth = "";
    private static String currentScreenHeight = "";
    private static String currentInterval = "";
    private static String currentChannels = "";

    private boolean doneSelecting = false;
    private boolean startedSelecting = false;

    private int completedSelections = 0;
    private int requiredSelections = 0;

    public static List<SelectionData> selectionList = new ArrayList<>();
    private boolean shouldClose = false;
    private BitmapPrintListWidget scrollWidget;

    public BitmapPrinterScreen() {
        super(Text.literal("Setup Bitmap Print"));
        Redstonecomptools.shouldApplyButtonStyle = true;

        setupGuiRender();
    }

    @Override
    protected void init() { // TODO make scroll list with entries
        super.init();
        this.nameField = new PlaceholderTextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, 50, 200, 20, Text.literal("Printer Label"));

        this.filePathField = new PlaceholderTextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, 80, 200, 20, Text.literal("Bitmap File Path"));

        this.screenWidthField = new PlaceholderTextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, 110, 100, 20, Text.literal("Screen Width"));
        this.screenHeightField = new PlaceholderTextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2, 110, 100, 20, Text.literal("Screen Height"));

        this.intervalField = new PlaceholderTextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, 140, 200, 20, Text.literal("Write Interval"));

        this.channelsField = new PlaceholderTextFieldWidget(MinecraftClient.getInstance().textRenderer, this.width / 2 - 100, 170, 200, 20, Text.literal("Amount of Channels"));
        // Scrollable List Here

        this.startSelect = ButtonWidget.builder(Text.literal("Start Next Selection"), button -> {
            startedSelecting = true;
            SelectionAbility.selectionContext = this;
            currentGuiText = "Selecting Bitmap Printer Input For Channel " + ((completedSelections / 2) + 1) + " On The " + (((completedSelections & 1) == 0) ? "X Axis" : "Y Axis");
            shouldClose = true;
            this.close();
        }).dimensions(this.width / 2 - 100, this.height - 50, 200, 20).build();

        this.startPrint = ButtonWidget.builder(Text.literal("Start Print"), button -> {
            if (currentName.isEmpty() || currentPath.isEmpty() || currentScreenWidth.isEmpty() || currentScreenHeight.isEmpty() || currentChannels.isEmpty() || currentInterval.isEmpty()) {
                return;
            }
            currentGuiText = "Printing...";
            BitmapPrinterCommand.finaliseExecution(currentPath, selectionList, Integer.parseInt(currentScreenWidth), Integer.parseInt(currentScreenHeight), Integer.parseInt(currentInterval), Integer.parseInt(currentChannels), MinecraftClient.getInstance().player.getWorld(), MinecraftClient.getInstance().player);
            shouldClose = true;
            this.close();
        }).dimensions(this.width / 2 - 100, this.height - 50, 200, 20).build();

        this.scrollWidget = new BitmapPrintListWidget(client, this.width, 265, 20, 100);
        scrollWidget.visible = false;

        this.addDrawableChild(nameField);
        this.addDrawableChild(filePathField);
        this.addDrawableChild(screenWidthField);
        this.addDrawableChild(screenHeightField);
        this.addDrawableChild(intervalField);
        this.addDrawableChild(channelsField);

        this.addDrawableChild(scrollWidget);

        this.addDrawableChild(startSelect);

        this.addDrawableChild(startPrint);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (startedSelecting) {
            for (Element element : this.children()) {
                ClickableWidget widget = (ClickableWidget) element;
                widget.visible = false;
            }
        }
        startSelect.visible = !doneSelecting;
        startPrint.visible = doneSelecting;
        if (!channelsField.getText().isEmpty() && !channelsField.getText().equals(channelsField.getPlaceholder())) {
            try {
                requiredSelections = Integer.parseInt(channelsField.getText()) * 2;
            } catch (NumberFormatException e) {
                RedstonecomptoolsClient.LOGGER.error(String.valueOf(e));
                return;
            }
        }

        if (!startedSelecting) {
            currentName = nameField.getText();
            currentPath = filePathField.getText();
            currentScreenWidth = screenWidthField.getText();
            currentScreenHeight = screenHeightField.getText();
            currentInterval = intervalField.getText();
            currentChannels = channelsField.getText();
        } else {
            scrollWidget.visible = true;
        }

        scrollWidget.updateChildren();

        super.render(context, mouseX, mouseY, delta);
    }

    public static void setupGuiRender() {
        if (!isRendererSetup) {
            HudRenderCallback.EVENT.register((context, tickDeltaManager) -> {
                if (shouldRender) {
                    context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of(currentGuiText), context.getScaledWindowWidth() / 2, 10, 0xFFFFFF);
                }
            });
            isRendererSetup = true;
        }
    }

    @Override
    public void recall(BlockPos blockPos, Color color, Vec3i size) {
        SelectionAbility.selectionContext = RedstonecomptoolsClient.defaultSelectionContext;

        completedSelections++;

        SelectionData data = new SelectionData(blockPos, color, size, currentName + "♅" + ((completedSelections / 2) + 1) + "♅" + (((completedSelections & 1) == 0) ? "y" : "x"), false);
        data.setOffset(1); // TODO add option to change offset
        data.context = this;
        selectionList.add(data);

        Redstonecomptools.shouldApplyButtonStyle = true;
        shouldRender = false;
        MinecraftClient.getInstance().setScreen(this);
        if (completedSelections == requiredSelections) {
            doneSelecting = true;
        }
    }

    @Override
    public void close() {
        if (shouldClose) {
            shouldClose = false;
            Redstonecomptools.shouldApplyButtonStyle = false;
            shouldRender = true;
            MinecraftClient.getInstance().setScreen(null);
        }
    }

    public static void finishPrint() {
        Toaster.sendToast(MinecraftClient.getInstance(), Text.literal("Bitmap Printer"), Text.literal("Finished Bitmap Print"));
        shouldRender = false;
        for (SelectionData data : selectionList) {
            BlockOverlayRenderer.removePrinterOverlay(data.context);
        }
        selectionList.clear();
    }
}