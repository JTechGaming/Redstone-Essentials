package me.jtech.redstone_essentials.client.utility;

import me.jtech.redstone_essentials.IO.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class Toaster { //TODO comment this
    public static void sendToast(MinecraftClient client, Text title, Text text, long duration) {
        if (!Config.use_toasts) {
            return;
        }
        client.getToastManager().add(
                SystemToast.create(client, new SystemToast.Type(500L), title, text)
        );
    }

    public static void sendToast(MinecraftClient client, Text title, Text text) {
        sendToast(client, title, text, 500L);
    }
}
