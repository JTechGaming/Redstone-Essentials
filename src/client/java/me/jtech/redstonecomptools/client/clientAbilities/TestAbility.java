package me.jtech.redstonecomptools.client.clientAbilities;

import org.lwjgl.glfw.GLFW;

public class TestAbility extends BaseAbility{
    public TestAbility() {
        super("Test Ability", true, GLFW.GLFW_KEY_J);
    }

    @Override
    public void postInit() {

    }

    @Override
    public void toggled(boolean state) {

    }

    @Override
    public void used() {

    }
}
