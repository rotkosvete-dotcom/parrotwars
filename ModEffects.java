package com.parrotwars.effect;

import com.parrotwars.ParrotWars;
import net.minecraft.potion.Effect;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, ParrotWars.MODID);

    public static final RegistryObject<Effect> PARROT_FORM = EFFECTS.register("parrot_form", ParrotFormEffect::new);
}
