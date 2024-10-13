package me.jtech.redstonecomptools.client;

import me.jtech.redstonecomptools.client.clientAbilities.BaseAbility;
import me.jtech.redstonecomptools.client.clientAbilities.DustPlaceAbility;
import me.jtech.redstonecomptools.client.clientAbilities.PingAbility;
import me.jtech.redstonecomptools.client.clientAbilities.SwapBlockTypeAbility;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class Abilities { //TODO comment this

    public static Abilities instance;





    public static final BaseAbility DUST_PLACE = register("dust_place", new DustPlaceAbility("dust_place"));
    public static final BaseAbility PING = register("ping_ability", new PingAbility("ping_ability"));
    public static final BaseAbility SWAP_BLOCK = register("swap_block", new SwapBlockTypeAbility("swap_block"));






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
