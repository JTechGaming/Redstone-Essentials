package me.jtech.redstone_essentials.client;

import me.jtech.redstone_essentials.client.clientAbilities.BaseAbility;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;

public class AbilityManager { //TODO comment this

    static SimpleRegistry<BaseAbility> registry;

    public static void init() {
        registry = FabricRegistryBuilder
                .createSimple(BaseAbility.class, Identifier.of("ability_register", "fabric_registry"))
                .attribute(RegistryAttribute.SYNCED)
                .buildAndRegister();
    }

    public static BaseAbility register(RegistryKey<BaseAbility> key, BaseAbility ability) {
        if (registry != null) {
            registry.add(key, ability, RegistryEntryInfo.DEFAULT);
        }
        return ability;
    }

    public static void initAbilities() {
        for (BaseAbility ability : registry) {
            ability.init();
        }
    }
}
