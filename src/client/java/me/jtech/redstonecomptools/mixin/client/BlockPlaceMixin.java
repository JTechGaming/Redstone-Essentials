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
        // Called when the player places a block
        PlayerEntity player = (PlayerEntity) placer; // Get a reference to the player

        // Check if player isn't null
        if (player != null) {
            // Only execute this code if the dust place ability is enabled
            if (Abilities.DUST_PLACE.toggled) {
                BlockPos dustLoc = pos.up(); // Get the location of the block on top of the block that was placed
                Block dustBlock = world.getBlockState(dustLoc).getBlock(); // Get the block in that location
                if (dustBlock == Blocks.AIR) { // Only change it if it is air
                    // Create a placementContext
                    ItemPlacementContext context = new ItemPlacementContext(player, Hand.MAIN_HAND, new ItemStack(Items.REDSTONE), new BlockHitResult(new Vec3d(dustLoc.getX(), dustLoc.getY(), dustLoc.getZ()), Direction.UP, dustLoc, false));
                    BlockState redstoneWireState = Blocks.REDSTONE_WIRE.getPlacementState(context); // Convert the placementContext into a blockstate

                    if (redstoneWireState != null && redstoneWireState.canPlaceAt(world, dustLoc)) { // Only execute if the dust is able to be placed at that location
                        placer.getWorld().setBlockState(dustLoc, Blocks.REDSTONE_WIRE.getPlacementState(context)); // Place the dust at the location
                    }
                }
            }
        }
    }
}
