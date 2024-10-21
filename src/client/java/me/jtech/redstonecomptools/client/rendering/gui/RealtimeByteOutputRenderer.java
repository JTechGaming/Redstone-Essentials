package me.jtech.redstonecomptools.client.rendering.gui;

import me.jtech.redstonecomptools.SelectionData;
import me.jtech.redstonecomptools.config.Config;
import me.jtech.redstonecomptools.utility.SelectionHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;

public class RealtimeByteOutputRenderer {
    private static boolean shouldRender = false;

    public static List<SelectionData> realtimeByteOutputList = new ArrayList<>();

    public void setup() {
        HudRenderCallback.EVENT.register((context, tickDeltaManager) -> {
            if (shouldRender && Config.rtbo_enabled) {
                MinecraftClient client = MinecraftClient.getInstance();
                context.fill(context.getScaledWindowWidth()-150, context.getScaledWindowHeight()-50, context.getScaledWindowWidth(), 50, 0xAF221B1B);
                context.drawCenteredTextWithShadow(client.textRenderer, "Byte Outputs", context.getScaledWindowWidth()-75, 60, 0xFFFFFF);
                int y = 0;
                if (realtimeByteOutputList.isEmpty()) {
                    context.drawCenteredTextWithShadow(client.textRenderer, "You have not created any byte", context.getScaledWindowWidth()-75, context.getScaledWindowHeight()/2-5, 0xFFFFFF);
                    context.drawCenteredTextWithShadow(client.textRenderer, "output selections yet.", context.getScaledWindowWidth()-75, context.getScaledWindowHeight()/2+5, 0xFFFFFF);
                }
                for (SelectionData realtimeByteOutput : realtimeByteOutputList) {
                    String data = String.valueOf(getData(realtimeByteOutput));
                    switch (Config.output_base_select) {
                        case HEX -> data = "0x".concat(Config.fill_to_min_size ? fillZeros(Integer.toHexString(Integer.parseInt(data)), Config.min_byte_size) : Integer.toHexString(Integer.parseInt(data)));
                        case BIN -> data = Config.fill_to_min_size ? fillZeros(Integer.toBinaryString(Integer.parseInt(data)), Config.min_byte_size) : Integer.toBinaryString(Integer.parseInt(data));
                        case OCT -> data = Config.fill_to_min_size ? fillZeros(Integer.toOctalString(Integer.parseInt(data)), Config.min_byte_size) : Integer.toOctalString(Integer.parseInt(data));
                    }
                    if (Config.use_output_labels) {
                        data = "(" + realtimeByteOutput.label + "):  " + data;
                    }
                    context.drawCenteredTextWithShadow(client.textRenderer, data, context.getScaledWindowWidth()-75, 75 + y, realtimeByteOutput.color.getRGB());
                    y = y + 10;
                }
            }
        });
    }

    private int getData(SelectionData realtimeByteOutput) {
        int x = realtimeByteOutput.blockPos.getX() + realtimeByteOutput.size.getX() - 1;
        int y = realtimeByteOutput.blockPos.getY() + realtimeByteOutput.size.getY() - 1;
        int z = realtimeByteOutput.blockPos.getZ() + realtimeByteOutput.size.getZ() - 1;

        SelectionHelper selectionHelper = new SelectionHelper(realtimeByteOutput.blockPos, new BlockPos(x, y, z), false);
        return selectionHelper.readData(MinecraftClient.getInstance().world, 1);
    }

    public static boolean isShouldRender() {
        return shouldRender;
    }

    public static void setShouldRender(boolean shouldRender) {
        RealtimeByteOutputRenderer.shouldRender = shouldRender;
    }

    public static String fillZeros(String input, int minAmount) {
        if (input.length()>=minAmount) {
            return input;
        }
        StringBuilder buffer = new StringBuilder(input);
        for (int i=0; i < minAmount-input.length(); i++) {
            buffer.insert(0, "0");
        }
        return buffer.toString();
    }
}
