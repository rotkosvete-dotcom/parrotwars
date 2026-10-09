package com.parrotwars;

import com.parrotwars.block.ModBlocks;
import com.parrotwars.entity.*;
import com.parrotwars.item.ModItems;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ParrotWars.MODID)
public class ParrotWars {
    public static final String MODID = "parrotwars";

    public ParrotWars() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        bus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GlobalEntityTypeAttributes.put(ModEntities.PARROT_SOLDIER.get(), ParrotSoldierEntity.createSoldierAttributes().build());
            GlobalEntityTypeAttributes.put(ModEntities.PARROT_SHOOTER.get(), ParrotShooterEntity.createShooterAttributes().build());
            GlobalEntityTypeAttributes.put(ModEntities.PARROT_MEDIC.get(), ParrotMedicEntity.createMedicAttributes().build());
            GlobalEntityTypeAttributes.put(ModEntities.PIGEON.get(), PigeonEntity.createPigeonAttributes().build());
        });
    }
}
