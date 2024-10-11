package me.jtech.redstonecomptools.client.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class Toaster { //TODO comment this
    public static void sendToast(MinecraftClient client, Text title, Text text) {
        client.getToastManager().add(
                SystemToast.create(client, new SystemToast.Type(1000L), title, text)
        );
    }
}
