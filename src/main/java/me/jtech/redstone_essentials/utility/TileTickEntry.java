package me.jtech.redstone_essentials.utility;

import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.TickPriority;

public class TileTickEntry {
    private final BlockPos position;
    private final Block block;
    private final int delay;
    private final TickPriority priority;

    public TileTickEntry(BlockPos position, Block block, int delay, TickPriority priority) {
        this.position = position;
        this.block = block;
        this.delay = delay;
        this.priority = priority;
    }

    public void execute(ServerWorld world) {
        block.scheduledTick(world.getBlockState(position), world, position, world.random);
    }

    public BlockPos getPosition() {
        return position;
    }

    public Block getBlock() {
        return block;
    }

    public int getDelay() {
        return delay;
    }

    public TickPriority getPriority() {
        return priority;
    }
}

