package me.jtech.redstonecomptools.mixin.client;

import me.jtech.redstonecomptools.Redstonecomptools;
import net.minecraft.client.gui.screen.ButtonTextures;
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
public abstract class GUIButtonMixin extends ClickableWidget { // Make the class abstract to avoid having to implement methods
    @Shadow @Final private static ButtonTextures TEXTURES; // Bring this variable from the PressableWidget class into scope

    public GUIButtonMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message); // Constructor is needed because we need this class to extend ClickableWidget
    }

    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    public Identifier renderInjected(Identifier texture) // Replace the drawGuiTexture texture with a custom one based on the outcome of this code
    {
        if (Redstonecomptools.shouldApplyButtonStyle) { // If the custom button style should be applied
            if (this.isHovered()) { // If the button is hovered
                return Identifier.of(Redstonecomptools.MOD_ID, "button_highlighted"); // Provide the custom highlighted texture
            }
            return Identifier.of(Redstonecomptools.MOD_ID, "button"); // Else, provide the custom regular texture
        }
        return this.TEXTURES.get(this.active, this.isSelected()); // If the custom button style shouldn't be applied, use the default code as seen in:
        /**
         * @Param Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V
         * (to jump to this reference, it is the same as in the @ModifyArg's target parameter above)
         * */
    }
}
