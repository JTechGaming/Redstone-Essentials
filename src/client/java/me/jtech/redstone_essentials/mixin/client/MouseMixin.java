package me.jtech.redstone_essentials.mixin.client;

import me.jtech.redstone_essentials.client.Redstone_Essentials_Client;
import me.jtech.redstone_essentials.client.editor.imgui.ImGuiImplementation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow private double x;
    @Shadow private double y;
    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;
    @Shadow private boolean hasResolutionChanged;
    @Final
    @Shadow private MinecraftClient client;

    @Inject(method = "isCursorLocked", at=@At("HEAD"), cancellable = true)
    public void isMouseGrabbed(CallbackInfoReturnable<Boolean> cir) {
        if (ImGuiImplementation.isActive()) {
            //cir.setReturnValue(ImGuiImplementation.imGuiImplGlfw.getMouseHandledBy() == CustomImGuiImplGlfw.MouseHandledBy.GAME);
        }
    }

    @Inject(method = {"onMouseButton", "onMouseScroll", "onCursorPos"}, at = @At("HEAD"), cancellable = true)
    public void onUseMouse(CallbackInfo ci) {
//        if (Flashback.isExporting()) {
//            ci.cancel();
//        }
    }

    @Inject(method = "lockCursor", at=@At("HEAD"), cancellable = true)
    public void grabMouse(CallbackInfo ci) {
        if (ImGuiImplementation.isActive() && !ImGuiImplementation.grabbed) {
            ci.cancel();
        }
    }

    @Inject(method = "unlockCursor", at=@At("HEAD"), cancellable = true)
    public void releaseMouse(CallbackInfo ci) {
        if (ImGuiImplementation.isActive() && ImGuiImplementation.grabbed) {
            ci.cancel();
        }
    }

    // Cancel hotbar scrolling if the editor is open and the mouse is not grabbed because i dont want you to scroll in the editor XD
    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), cancellable = true)
    private void packified$cancelHotbarScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (Redstone_Essentials_Client.shouldRender && !ImGuiImplementation.grabbed) {
            ci.cancel();
        }
    }
}
