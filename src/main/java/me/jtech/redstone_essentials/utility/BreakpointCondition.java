package me.jtech.redstone_essentials.utility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class BreakpointCondition {
    private final BlockPos position;
    private final Block block;
    public final Map<Property<?>, String> conditions = new HashMap<>(); // Custom conditions

    public BreakpointCondition(BlockPos position, Block block) {
        this.position = position;
        this.block = block;
    }

    public void addCondition(Property<?> property, String value) {
        conditions.put(property, value);
    }

    public boolean matches(BlockState state) {
        for (Map.Entry<Property<?>, String> entry : conditions.entrySet()) {
            if (!state.get(entry.getKey()).toString().equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public BlockPos getPosition() { return position; }
    public Block getBlock() { return block; }
}
