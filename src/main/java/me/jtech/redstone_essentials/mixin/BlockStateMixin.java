package me.jtech.redstone_essentials.mixin;

import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.debugger.DebugManager;
import me.jtech.redstone_essentials.utility.BlockChange;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class BlockStateMixin {
    @Inject(method = "updateNeighbors(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V", at = @At("HEAD"))
    private void onNeighborUpdate(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, CallbackInfo ci) {
        if (!world.isClient()) {
            if (DebugManager.debugSessions == null) {
                return;
            }
            int tick = Redstone_Essentials.getInstance().getServer().getTicks();
            BlockChange change = new BlockChange(tick, pos, world.getBlockState(pos), world.getBlockState(pos));
            DebugManager.activeDebugSessions.values().forEach(debuggerSession -> debuggerSession.recordChange(tick, change));
        }
    }

    @Inject(method = "neighborUpdate(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V", at = @At("HEAD"))
    private void neighborUpdate(World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        if (!world.isClient()) {
            if (DebugManager.debugSessions == null) {
                return;
            }
            int tick = Redstone_Essentials.getInstance().getServer().getTicks();
            BlockChange change = new BlockChange(tick, pos, world.getBlockState(pos), world.getBlockState(pos));
            DebugManager.activeDebugSessions.values().forEach(debuggerSession -> debuggerSession.recordChange(tick, change));
        }
    }
}
