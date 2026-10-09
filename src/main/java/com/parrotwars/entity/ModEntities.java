package com.parrotwars.entity;

import com.parrotwars.ParrotWars;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, ParrotWars.MODID);

    public static final RegistryObject<EntityType<ParrotSoldierEntity>> PARROT_SOLDIER = ENTITIES.register("parrot_soldier",
            () -> EntityType.Builder.<ParrotSoldierEntity>of(ParrotSoldierEntity::new, EntityClassification.CREATURE)
                    .sized(0.5F, 0.9F).clientTrackingRange(8).build("parrot_soldier"));

    public static final RegistryObject<EntityType<PigeonEntity>> PIGEON = ENTITIES.register("pigeon",
            () -> EntityType.Builder.<PigeonEntity>of(PigeonEntity::new, EntityClassification.MONSTER)
                    .sized(0.5F, 0.9F).clientTrackingRange(8).build("pigeon"));
}
