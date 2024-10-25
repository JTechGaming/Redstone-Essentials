package me.jtech.redstonecomptools.client.utility;

import me.jtech.redstonecomptools.IO.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class Toaster { //TODO comment this
    public static void sendToast(MinecraftClient client, Text title, Text text) {
        if (!Config.use_toasts) {
            return;
        }
        client.getToastManager().add(
                SystemToast.create(client, new SystemToast.Type(250L), title, text)
        );
    }
}
