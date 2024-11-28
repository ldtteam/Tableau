package net.ldtteam.example;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.Mod;

/**
 * Example mod class.
 */
@Mod("example")
public class ExampleMod {

    /**
     * Creates a new example mod.
     */
    public ExampleMod() {
        System.out.println(Minecraft.getInstance().getClass().getName());
    }
}
