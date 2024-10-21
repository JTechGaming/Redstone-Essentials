package me.jtech.redstonecomptools.client.utility;

import me.jtech.redstonecomptools.utility.IClientSelectionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

public class DefaultSelectionContext implements IClientSelectionContext {
    @Override
    public void recall(BlockPos blockPos, Color color, Vec3i size) {
        System.out.println("No Selection Context in use!!!");
    }
}
