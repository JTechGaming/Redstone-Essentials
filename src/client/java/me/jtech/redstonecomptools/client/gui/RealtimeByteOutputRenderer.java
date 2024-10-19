package me.jtech.redstonecomptools.client.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class RealtimeByteOutputRenderer {
    private static boolean shouldRender = false;

    public static void setup() {
        HudRenderCallback.EVENT.register((context, tickDeltaManager) -> {
            if (shouldRender) {
                MinecraftClient client = MinecraftClient.getInstance();
                context.drawTextWithShadow(client.textRenderer, "0101010101010101", context.getScaledWindowWidth()-100, context.getScaledWindowHeight()/2, 0xFFFFFF);
            }
        });
    }

    public static boolean isShouldRender() {
        return shouldRender;
    }

    public static void setShouldRender(boolean shouldRender) {
        RealtimeByteOutputRenderer.shouldRender = shouldRender;
    }
}
