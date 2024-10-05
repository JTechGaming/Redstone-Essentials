package me.jtech.redstonecomptools.client;

import me.jtech.redstonecomptools.client.clientAbilities.BaseAbility;
import me.jtech.redstonecomptools.client.clientAbilities.DustPlaceAbility;
import me.jtech.redstonecomptools.client.clientAbilities.TestAbility;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;

public class Abilities {

    public static Abilities instance;





    public static final BaseAbility DUST_PLACE = register("dust_place", new DustPlaceAbility());
    public static final BaseAbility TEST_ABILITY = register("test_ability", new TestAbility());






    public static BaseAbility register(String id, BaseAbility ability) {
        return register(Identifier.of("ability_register", id), ability);
    }

    public static BaseAbility register(Identifier id, BaseAbility ability) {
        return AbilityManager.register(RegistryKey.of(AbilityManager.registry.getKey(), id), ability);
    }

    public Abilities() {
        instance = this;
    }

    public static Abilities getInstance() {
        return instance;
    }
}
