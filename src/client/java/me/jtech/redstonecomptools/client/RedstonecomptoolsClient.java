package me.jtech.redstonecomptools.client;

import me.jtech.redstonecomptools.client.axiomExtensions.ServiceHelper;
import me.jtech.redstonecomptools.client.axiomExtensions.tools.forceNeighborUpdatesTool;
import net.fabricmc.api.ClientModInitializer;
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

        LOGGER.info("Registering forceNeighborUpdatesTool...");
        ServiceHelper.getToolRegistryService().register(new forceNeighborUpdatesTool());
    }
}
