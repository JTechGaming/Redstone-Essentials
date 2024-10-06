package me.jtech.redstonecomptools.client;

import me.jtech.redstonecomptools.client.clientAbilities.BaseAbility;
import me.jtech.redstonecomptools.client.clientAbilities.DustPlaceAbility;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class Abilities {

    public static Abilities instance;





    public static final BaseAbility DUST_PLACE = register("dust_place", new DustPlaceAbility());






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
