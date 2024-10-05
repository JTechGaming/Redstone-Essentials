package me.jtech.redstonecomptools.client.clientAbilities;

import org.lwjgl.glfw.GLFW;

public class DustPlaceAbility extends BaseAbility{
    public DustPlaceAbility() {
        super("Dust Place", false, GLFW.GLFW_KEY_O);
    }

    @Override
    public void postInit() {

    }

    @Override
    public void toggled(boolean state) {
        if (state) {
            System.out.println("enabled dustplace ability");
        } else {
            System.out.println("disabled dustplace ability");
        }
    }

    @Override
    public void used() {

    }
}
