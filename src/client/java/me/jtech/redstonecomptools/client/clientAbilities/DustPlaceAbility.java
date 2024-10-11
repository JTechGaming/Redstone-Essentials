package me.jtech.redstonecomptools.client.clientAbilities;

import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DustPlaceAbility extends BaseAbility{
    public DustPlaceAbility(String identifier) {
        super("Dust Place", false, GLFW.GLFW_KEY_O, true, false, Identifier.of(identifier)); // Set the settings and default values for this ability
    }

    @Override
    public void postInit() { }

    @Override
    public void toggled(boolean state) { }

    @Override
    public void used() { }
}
