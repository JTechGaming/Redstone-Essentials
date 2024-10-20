package me.jtech.redstonecomptools;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

public class RealtimeByteOutput {
    public BlockPos blockPos;
    public Color color;
    public Vec3i size;
    public String label;

    public RealtimeByteOutput(BlockPos blockPos, Color color, Vec3i size, String label) {
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
        this.label = label;
    }
}
