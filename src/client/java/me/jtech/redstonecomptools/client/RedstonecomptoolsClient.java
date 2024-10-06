package me.jtech.redstonecomptools.client;

import me.jtech.redstonecomptools.client.axiomExtensions.ServiceHelper;
import me.jtech.redstonecomptools.client.axiomExtensions.tools.forceNeighborUpdatesTool;
import me.jtech.redstonecomptools.client.qolTools.SignalStrengthGiver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedstonecomptoolsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("redstonecomptools");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initialising Registers...");
        AbilityManager.init();
        Abilities abilities = Abilities.getInstance();
        AbilityManager.initAbilities();

        LOGGER.info("Setting up QOL features...");
        // Setup keybinds for barrel and shulker giver
        SignalStrengthGiver.setupKeybinds();

        LOGGER.info("Registering axiom extension tools...");
        ServiceHelper.getToolRegistryService().register(new forceNeighborUpdatesTool());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SignalStrengthGiver.processBarrel();
            SignalStrengthGiver.processShulker();
        });

    }
}
