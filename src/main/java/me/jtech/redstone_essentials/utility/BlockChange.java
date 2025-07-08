package me.jtech.redstone_essentials.utility;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class BlockChange {
    private final long tickNumber; // Store the tick this change occurred in
    private final BlockPos position;
    private final BlockState oldState;
    private final BlockState newState;

    public BlockChange(long tickNumber, BlockPos position, BlockState oldState, BlockState newState) {
        this.tickNumber = tickNumber;
        this.position = position;
        this.oldState = oldState;
        this.newState = newState;
    }

    public long getTickNumber() { return tickNumber; }
    public BlockPos getPosition() { return position; }
    public BlockState getOldState() { return oldState; }
    public BlockState getNewState() { return newState; }
}

