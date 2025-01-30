package me.jtech.redstone_essentials.client.IO;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.jtech.redstone_essentials.IO.ColorTypeAdapter;
import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.client.rendering.BlockOverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionStorage {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .create();
    private static final Path CONFIG_FILE = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/redstone_essentials/sessions.json");

    public static Data retreiveSelectionsForServer(String ip) {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                Type type = new TypeToken<Map<String, Data>>() {
                }.getType();
                Map<String, Data> data = GSON.fromJson(reader, type);
                if (data == null || data.isEmpty() || !data.containsKey(ip)) {
                    return null;
                }
                return data.get(ip);
            } catch (IOException e) {
                Redstone_Essentials_Client.LOGGER.error(String.valueOf(e));
            }
        }
        return null;
    }

    public static void storeSelectionsForServer(Data data, String ip) {
        Map<String, Data> map = new HashMap<>();
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                Type type = new TypeToken<Map<String, Data>>() {
                }.getType();
                map = GSON.fromJson(reader, type); // Load the existing map
            } catch (IOException e) {
                Redstone_Essentials_Client.LOGGER.error("Error reading session storage: {}", String.valueOf(e));
            }
        }
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(ip, data);
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(map, writer); // Write the updated map with all IP entries
        } catch (IOException e) {
            Redstone_Essentials_Client.LOGGER.error("Error saving session storage: {}", String.valueOf(e));
        }
    }

    public static class Data {
        public List<BlockOverlayRenderer> overlays;
        public List<BlockPos> overlayPositions;
        public List<BlockOverlayRenderer> selectionOverlays;

        public Data(List<BlockOverlayRenderer> overlays, List<BlockPos> overlayPositions, List<BlockOverlayRenderer> selectionOverlays) {
            this.overlays = overlays;
            this.overlayPositions = overlayPositions;
            this.selectionOverlays = selectionOverlays;
        }
    }
}
