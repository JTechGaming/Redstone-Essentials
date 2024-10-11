package me.jtech.redstonecomptools.client.clientAbilities;

import org.lwjgl.glfw.GLFW;

public class DustPlaceAbility extends BaseAbility{
    public DustPlaceAbility() {
        super("Dust Place", false, GLFW.GLFW_KEY_O); // Set the settings and default values for this ability
    }

    @Override
    public void postInit() { }

    @Override
    public void toggled(boolean state) { }

    @Override
    public void used() { }
}
