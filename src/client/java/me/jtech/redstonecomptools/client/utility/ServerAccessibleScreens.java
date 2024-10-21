package me.jtech.redstonecomptools.client.utility;

import me.jtech.redstonecomptools.client.rendering.screen.BitmapPrinter.BitmapPrinterScreen;
import net.minecraft.client.gui.screen.Screen;

import java.util.HashMap;
import java.util.Map;

public class ServerAccessibleScreens {
    public static Map<Integer, Screen> screenList = new HashMap<>();

    private static final Screen BITMAP_PRINTER_SCREEN = register(new BitmapPrinterScreen());

    private static Screen register(Screen screen) {
        screenList.put(screenList.size(), screen);
        return screenList.get(screenList.size()-1);
    }
}
