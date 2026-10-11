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

    // ---- попугаи ----
    public static final RegistryObject<EntityType<ParrotSoldierEntity>> PARROT_SOLDIER = ENTITIES.register("parrot_soldier",
            () -> EntityType.Builder.<ParrotSoldierEntity>of(ParrotSoldierEntity::new, EntityClassification.CREATURE)
                    .sized(0.5F, 0.9F).clientTrackingRange(8).build("parrot_soldier"));

    public static final RegistryObject<EntityType<ParrotShooterEntity>> PARROT_SHOOTER = ENTITIES.register("parrot_shooter",
            () -> EntityType.Builder.<ParrotShooterEntity>of(ParrotShooterEntity::new, EntityClassification.CREATURE)
                    .sized(0.5F, 0.9F).clientTrackingRange(8).build("parrot_shooter"));

    public static final RegistryObject<EntityType<ParrotMedicEntity>> PARROT_MEDIC = ENTITIES.register("parrot_medic",
            () -> EntityType.Builder.<ParrotMedicEntity>of(ParrotMedicEntity::new, EntityClassification.CREATURE)
                    .sized(0.5F, 0.9F).clientTrackingRange(8).build("parrot_medic"));

    public static final RegistryObject<EntityType<ParrotTankEntity>> PARROT_TANK = ENTITIES.register("parrot_tank",
            () -> EntityType.Builder.<ParrotTankEntity>of(ParrotTankEntity::new, EntityClassification.CREATURE)
                    .sized(1.4F, 1.3F).clientTrackingRange(10).build("parrot_tank"));

    public static final RegistryObject<EntityType<ParrotArtilleryEntity>> PARROT_ARTILLERY = ENTITIES.register("parrot_artillery",
            () -> EntityType.Builder.<ParrotArtilleryEntity>of(ParrotArtilleryEntity::new, EntityClassification.CREATURE)
                    .sized(1.3F, 1.2F).clientTrackingRange(10).build("parrot_artillery"));

    // ---- голуби ----
    public static final RegistryObject<EntityType<PigeonEntity>> PIGEON = ENTITIES.register("pigeon",
            () -> EntityType.Builder.<PigeonEntity>of(PigeonEntity::new, EntityClassification.MONSTER)
                    .sized(0.5F, 0.9F).clientTrackingRange(8).build("pigeon"));

    public static final RegistryObject<EntityType<PigeonBomberEntity>> PIGEON_BOMBER = ENTITIES.register("pigeon_bomber",
            () -> EntityType.Builder.<PigeonBomberEntity>of(PigeonBomberEntity::new, EntityClassification.MONSTER)
                    .sized(0.5F, 0.9F).clientTrackingRange(8).build("pigeon_bomber"));

    public static final RegistryObject<EntityType<PigeonSniperEntity>> PIGEON_SNIPER = ENTITIES.register("pigeon_sniper",
            () -> EntityType.Builder.<PigeonSniperEntity>of(PigeonSniperEntity::new, EntityClassification.MONSTER)
                    .sized(0.5F, 0.9F).clientTrackingRange(8).build("pigeon_sniper"));

    public static final RegistryObject<EntityType<PigeonTankEntity>> PIGEON_TANK = ENTITIES.register("pigeon_tank",
            () -> EntityType.Builder.<PigeonTankEntity>of(PigeonTankEntity::new, EntityClassification.MONSTER)
                    .sized(1.4F, 1.3F).clientTrackingRange(10).build("pigeon_tank"));

    public static final RegistryObject<EntityType<PigeonArtilleryEntity>> PIGEON_ARTILLERY = ENTITIES.register("pigeon_artillery",
            () -> EntityType.Builder.<PigeonArtilleryEntity>of(PigeonArtilleryEntity::new, EntityClassification.MONSTER)
                    .sized(1.3F, 1.2F).clientTrackingRange(10).build("pigeon_artillery"));
}
