package me.jtech.redstonecomptools.client.utility;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

public interface IClientSelectionContext {
    void recall(BlockPos blockPos, Color color, Vec3i size);
}
