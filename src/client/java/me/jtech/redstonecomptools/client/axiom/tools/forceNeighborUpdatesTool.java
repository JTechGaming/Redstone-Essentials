package me.jtech.redstonecomptools.client.axiom.tools;

import com.moulberry.axiomclientapi.CustomTool;
import com.moulberry.axiomclientapi.Effects;
import com.moulberry.axiomclientapi.pathers.BallShape;
import com.moulberry.axiomclientapi.pathers.ToolPatherUnique;
import com.moulberry.axiomclientapi.regions.BlockRegion;
import com.moulberry.axiomclientapi.regions.BooleanRegion;
import com.moulberry.axiomclientapi.service.ToolService;
import imgui.ImGui;
import me.jtech.redstonecomptools.client.axiom.ServiceHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

//TODO this doesnt work yet
public class forceNeighborUpdatesTool implements CustomTool { //TODO comment this

    private final BlockRegion blockRegion = ServiceHelper.createBlockRegion();
    private final BooleanRegion previewRegion = ServiceHelper.createBooleanRegion();
    private ToolPatherUnique pather = null;
    private boolean usingTool = false;

    private final int[] radius = new int[]{10};
    private int oldRadius = -1;

    @Override
    public void reset() {
        this.blockRegion.clear();
        this.pather = null;
        this.usingTool = false;
    }

    @Override
    public void render(Camera camera, float tickDelta, long time, MatrixStack poseStack, Matrix4f projection) {
        ToolService toolService = ServiceHelper.getToolService();

        if (!this.usingTool) {
            BlockHitResult hitResult = toolService.raycastBlock();
            if (hitResult == null) {
                return;
            }

            int radius = this.radius[0];
            if (this.oldRadius != radius) {
                this.oldRadius = radius;
                this.previewRegion.clear();
                BallShape.SPHERE.fillRegion(this.previewRegion, radius);
            }

            this.previewRegion.render(camera, Vec3d.of(hitResult.getBlockPos()), poseStack,
                    projection, time, Effects.BLUE);
        } else if (!ServiceHelper.getToolService().isMouseDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            toolService.pushBlockRegionChange(this.blockRegion);
            this.reset();
        } else {
            //BlockState block = toolService.getActiveBlock();
            /*this.pather.update((x, y, z) -> {
                this.blockRegion.addBlock(x, y, z, block);
            });*/
            this.pather.update((x, y, z) -> {
                BlockPos bPos = new BlockPos(x, y, z);
                MinecraftClient client = MinecraftClient.getInstance();
                ClientWorld world = client.world;
                assert world != null;
                BlockState bState = world.getBlockState(new BlockPos(x, y, z));
                bState.updateNeighbors(world, bPos, Block.NOTIFY_ALL, 3);
                Block b = bState.getBlock();
                /*if (b.equals(Blocks.OBSERVER)) {
                    ObserverBlock observerBlock = (ObserverBlock) b;
                    if (world.isEmittingRedstonePower(bPos, bState.get(Properties.FACING))) {
                        Direction direction = bState.get(Properties.FACING);
                        BlockState obsState = observerBlock.getDefaultState();
                        BlockState tempBlock = Blocks.AIR.getDefaultState();
                        world.setBlockState(bPos, tempBlock, Block.NOTIFY_ALL, 2);
                        world.setBlockState(bPos, obsState.with(Properties.FACING, direction).with(Properties.POWERED, Boolean.FALSE), Block.NOTIFY_ALL, 2);
                    } else {
                        Direction direction = bState.get(Properties.FACING);
                        BlockState obsState = observerBlock.getDefaultState();
                        BlockState tempBlock = Blocks.AIR.getDefaultState();
                        world.setBlockState(bPos, tempBlock, Block.NOTIFY_ALL, 2);
                        world.setBlockState(bPos, obsState.with(Properties.FACING, direction).with(Properties.POWERED, Boolean.TRUE), Block.NOTIFY_ALL, 2);
                    }
                }*/
                world.updateNeighborsAlways(bPos, b);
                world.updateListeners(bPos, bState, bState, 3);
                world.updateListeners(bPos, bState, bState, 1);
                world.updateListeners(bPos, bState, bState, 2);
                world.updateListeners(bPos, bState, bState, 4);
            });

            float opacity = (float) Math.sin(time / 1000000f / 50f / 8f);
            this.blockRegion.render(camera, Vec3d.ZERO, poseStack, projection,
                    0.75f + opacity*0.25f, 0.3f - opacity*0.2f);
        }
    }

    @Override
    public boolean callUseTool() {
        this.reset();
        this.pather = ServiceHelper.createToolPatherUnique(this.radius[0], BallShape.SPHERE);
        this.usingTool = true;
        return true;
    }

    @Override
    public boolean callConfirm() {
        return CustomTool.super.callConfirm();
    }

    @Override
    public void displayImguiOptions() {
        ImGui.sliderInt("Radius", this.radius, 0, 32);
    }

    @Override
    public String name() {
        return "Force Neighbor Updates";
    }
}
