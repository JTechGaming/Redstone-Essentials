package me.jtech.redstonecomptools.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.jtech.redstonecomptools.Redstonecomptools;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PressableWidget.class)
public abstract class GUIButtonMixin extends ClickableWidget {
    @Shadow @Final private static ButtonTextures TEXTURES;

    public GUIButtonMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    public Identifier renderInjected(Identifier texture)
    {
        if (Redstonecomptools.shouldApplyButtonStyle) {
            if (this.isHovered()) {
                return Identifier.of(Redstonecomptools.MOD_ID, "button_highlighted");
            }
            return Identifier.of(Redstonecomptools.MOD_ID, "button");
        }
        return this.TEXTURES.get(this.active, this.isSelected());
    }
}
