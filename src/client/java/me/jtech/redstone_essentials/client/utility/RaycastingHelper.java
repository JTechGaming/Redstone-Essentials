package me.jtech.redstone_essentials.client.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class RaycastingHelper {
    public static BlockPos performRaycast(MinecraftClient client) {
        // Get the player's position and rotation
        Vec3d cameraPos = client.player.getCameraPosVec(1.0F); // Get the player's camera position
        Vec3d lookVec = client.player.getRotationVec(1.0F); // Get the player's look direction

        // Create the end position for the ray
        Vec3d endPos = cameraPos.add(lookVec.multiply(40.0));

        // Perform the ray trace
        BlockHitResult blockHitResult = client.world.raycast(new RaycastContext(
                cameraPos, endPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE, // Ignore fluids
                client.player
        ));

        // Check if a block was hit
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            return blockHitResult.getBlockPos();
        }

        return null;
    }
}
