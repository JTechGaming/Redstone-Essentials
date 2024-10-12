package me.jtech.redstonecomptools.utility;

import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SelectionHelper {
    private final BlockPos pos1;
    private final BlockPos pos2;
    private final boolean isVertical;
    private final Axis selectionAxis;

    public enum Mode {
        READ, WRITE
    }

    public enum Axis {
        X, Y, Z
    }

    public SelectionHelper(BlockPos pos1, BlockPos pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;

        // Determine if it's vertical (Y axis) or horizontal (X or Z axis)
        if (pos1.getX() == pos2.getX() && pos1.getZ() == pos2.getZ()) {
            isVertical = true;
            selectionAxis = Axis.Y;
        } else if (pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ()) {
            isVertical = false;
            selectionAxis = Axis.X;
        } else if (pos1.getY() == pos2.getY() && pos1.getX() == pos2.getX()) {
            isVertical = false;
            selectionAxis = Axis.Z;
        } else {
            throw new IllegalArgumentException("Positions do not form a valid vertical or horizontal line.");
        }
    }

    // Write data into the selection (as redstone blocks and air)
    public void writeData(World world, int data, Mode mode) {
        // Loop through the range between pos1 and pos2 based on the axis
        int length = getLength();
        for (int i = 0; i < length; i++) {
            boolean isBitSet = ((data >> i) & 1) == 1; // Extract the i-th bit from the data

            BlockPos targetPos = getTargetPos(i);
            if (mode == Mode.WRITE) {
                if (isBitSet) {
                    world.setBlockState(targetPos, Blocks.REDSTONE_BLOCK.getDefaultState());
                } else {
                    world.setBlockState(targetPos, Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    // Read data from the selection
    public int readData(World world) {
        int result = 0;
        int length = getLength();

        for (int i = 0; i < length; i++) {
            BlockPos targetPos = getTargetPos(i);
            if (isBitSet(world, targetPos)) {
                result |= (1 << i); // Set the i-th bit in result
            }
        }
        return result;
    }

    // Determine the length of the selection
    private int getLength() {
        if (isVertical) {
            return Math.abs(pos2.getY() - pos1.getY()) + 1;
        } else if (selectionAxis == Axis.X) {
            return Math.abs(pos2.getX() - pos1.getX()) + 1;
        } else { // Axis.Z
            return Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        }
    }

    // Get the position for a specific index in the selection
    private BlockPos getTargetPos(int index) {
        if (isVertical) {
            int minY = Math.min(pos1.getY(), pos2.getY());
            return new BlockPos(pos1.getX(), minY + index, pos1.getZ());
        } else if (selectionAxis == Axis.X) {
            int minX = Math.min(pos1.getX(), pos2.getX());
            return new BlockPos(minX + index, pos1.getY(), pos1.getZ());
        } else { // Axis.Z
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            return new BlockPos(pos1.getX(), pos1.getY(), minZ + index);
        }
    }

    // Check if the block at the target position represents a set bit (1)
    private boolean isBitSet(World world, BlockPos pos) {
        return world.getBlockState(pos).isOf(Blocks.REDSTONE_BLOCK) ||
                (world.getBlockState(pos).getBlock() instanceof RedstoneLampBlock && world.getBlockState(pos).get(RedstoneLampBlock.LIT));
    }
}

