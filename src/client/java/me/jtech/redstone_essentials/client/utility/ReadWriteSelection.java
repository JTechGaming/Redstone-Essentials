package me.jtech.redstone_essentials.client.utility;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.SelectionData;
import me.jtech.redstone_essentials.client.clientAbilities.RealtimeByteOutputAbility;
import me.jtech.redstone_essentials.client.clientAbilities.SelectionAbility;
import me.jtech.redstone_essentials.networking.InfoPackets;
import me.jtech.redstone_essentials.networking.payloads.c2s.C2SInfoPacket;
import me.jtech.redstone_essentials.utility.IClientSelectionContext;
import me.jtech.redstone_essentials.utility.SelectionContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReadWriteSelection implements IClientSelectionContext {
    public static int CONTEXT = SelectionContext.register(new ReadWriteSelection());

    public static void startSelection() {
        SelectionAbility.selectionContext = CONTEXT;
    }

    public static String mode = "read";

    @Override
    public void recall(BlockPos blockPos, Color color, Vec3i size, int id, boolean wasModified) {
        SelectionAbility.selectionContext = Redstone_Essentials.getInstance().DEFAULT_CONTEXT;
        List<SelectionData> selections = new ArrayList<>();
        selections.add(new SelectionData(blockPos, color, size, "", true, MinecraftClient.getInstance().player.getName().getString()));
        ClientPlayNetworking.send(new C2SInfoPacket(InfoPackets.getInt(InfoPackets.C2S.RETURN_RW_BIN), mode, "", "", selections));
        mode = "read";
    }
}