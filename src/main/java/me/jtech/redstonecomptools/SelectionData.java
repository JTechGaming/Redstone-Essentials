package me.jtech.redstonecomptools;

import me.jtech.redstonecomptools.utility.IClientSelectionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

public class SelectionData {
    public BlockPos blockPos;
    public Color color;
    public Vec3i size;
    public String label;
    public boolean isRTBO;
    public int offset = 1;
    public boolean isInverted = false;
    public int context;
    public int id;

    public SelectionData(BlockPos blockPos, Color color, Vec3i size, String label, boolean isRTBO) {
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
        this.label = label;
        this.isRTBO = isRTBO;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isRTBO() {
        return isRTBO;
    }

    public void setRTBO(boolean RTBO) {
        isRTBO = RTBO;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Vec3i getSize() {
        return size;
    }

    public void setSize(Vec3i size) {
        this.size = size;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public boolean isInverted() {
        return isInverted;
    }

    public void setInverted(boolean inverted) {
        isInverted = inverted;
    }

    public int getContext() {
        return context;
    }

    public void setContext(int context) {
        this.context = context;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
