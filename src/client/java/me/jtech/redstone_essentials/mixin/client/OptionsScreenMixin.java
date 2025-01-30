package me.jtech.redstone_essentials.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindScreen;
import me.jtech.redstone_essentials.client.rendering.screen.rtbo.RTBOScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;addBody(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;"))
    private void addCustomButton(CallbackInfo ci, @Local GridWidget.Adder adder) {
        adder.add(ButtonWidget.builder(Text.literal("Dynamic Keys"), (button) -> {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            MinecraftClient.getInstance().setScreen(
                    new KeybindScreen(currentScreen)
            );
        }).build());
        adder.add(ButtonWidget.builder(Text.literal("RTBO Selections"), (button) -> {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            MinecraftClient.getInstance().setScreen(
                    new RTBOScreen(currentScreen)
            );
        }).build());
    }
}
