package me.jtech.redstone_essentials.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.jtech.redstone_essentials.client.rendering.screen.keybinds.KeybindScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(GameMenuScreen.class)
public abstract class PauzeScreenMixin extends Screen { //TODO comment this

    @Shadow protected abstract ButtonWidget createButton(Text text, Supplier<Screen> screenSupplier);

    protected PauzeScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z"))
    private void addCustomButton(CallbackInfo ci, @Local GridWidget.Adder adder) {
        adder.add(ButtonWidget.builder(Text.literal("Dynamic Keys"), (button) -> {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            MinecraftClient.getInstance().setScreen(
                    new KeybindScreen(currentScreen)
            );
        }).width(204).build(), 2);
    }
}
