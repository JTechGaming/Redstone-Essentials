package me.jtech.redstone_essentials.client.clientAbilities;

import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DustPlaceAbility extends BaseAbility{ //TODO comment this
    public DustPlaceAbility(String identifier) {
        super("Dust Place", false, GLFW.GLFW_KEY_O, true, false, Identifier.of("redstone_essentials", identifier)); // Set the settings and default values for this ability
    }

    @Override
    public void postInit() { }

    @Override
    public void toggled(boolean state) { }

    @Override
    public void used() { }
}
