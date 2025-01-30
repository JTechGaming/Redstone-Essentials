package me.jtech.redstone_essentials.client.axiom.tools;

import com.moulberry.axiomclientapi.CustomTool;
import com.moulberry.axiomclientapi.Effects;
import com.moulberry.axiomclientapi.pathers.BallShape;
import com.moulberry.axiomclientapi.pathers.ToolPatherUnique;
import com.moulberry.axiomclientapi.regions.BlockRegion;
import com.moulberry.axiomclientapi.regions.BooleanRegion;
import com.moulberry.axiomclientapi.service.ToolService;
import imgui.ImGui;
import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.client.axiom.ServiceHelper;
import me.jtech.redstone_essentials.networking.payloads.c2s.C2SInfoPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

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
                if (Redstone_Essentials_Client.packetsEnabled)
                    ClientPlayNetworking.send(new C2SInfoPacket(1, Registries.ITEM.getId(b.asItem()).getPath(), bPos.getX()+"♅"+bPos.getY()+"♅"+bPos.getZ()+"♅", "", new ArrayList<>()));
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
