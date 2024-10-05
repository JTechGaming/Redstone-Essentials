package me.jtech.redstonecomptools.mixin.client;

import me.jtech.redstonecomptools.client.Abilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockPlaceMixin {
    @Inject(method = "onPlaced", at = @At("TAIL"))
    private void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {

        PlayerEntity player = (PlayerEntity) placer;

        if (player != null) {
            if (Abilities.DUST_PLACE.toggled) {
                BlockPos dustLoc = pos.up();
                Block dustBlock = world.getBlockState(dustLoc).getBlock();
                if (dustBlock == Blocks.AIR) {
                    ItemPlacementContext context = new ItemPlacementContext(player, Hand.MAIN_HAND, new ItemStack(Items.REDSTONE), new BlockHitResult(new Vec3d(dustLoc.getX(), dustLoc.getY(), dustLoc.getZ()), Direction.UP, dustLoc, false));
                    BlockState redstoneWireState = Blocks.REDSTONE_WIRE.getPlacementState(context);

                    if (redstoneWireState != null && redstoneWireState.canPlaceAt(world, dustLoc)) {
                        placer.getWorld().setBlockState(dustLoc, Blocks.REDSTONE_WIRE.getPlacementState(context));
                    }
                }
            }
        }
    }
}
