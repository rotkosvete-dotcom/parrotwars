package com.parrotwars.client;

import com.parrotwars.ParrotWars;
import com.parrotwars.entity.ModEntities;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ParrotWars.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    private static ResourceLocation mod(String path) {
        return new ResourceLocation(ParrotWars.MODID, path);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        final ResourceLocation helmetOlive = mod("textures/entity/helmet_olive.png");
        final ResourceLocation helmetBrown = mod("textures/entity/helmet_brown.png");
        final ResourceLocation helmetWhite = mod("textures/entity/helmet_white.png");
        final ResourceLocation helmetDark = mod("textures/entity/helmet_dark.png");
        final ResourceLocation pigeon = mod("textures/entity/pigeon.png");
        final ResourceLocation pigeonDark = mod("textures/entity/pigeon_dark.png");
        final ResourceLocation tankParrot = mod("textures/entity/tank_parrot.png");
        final ResourceLocation tankPigeon = mod("textures/entity/tank_pigeon.png");
        final ResourceLocation artParrot = mod("textures/entity/artillery_parrot.png");
        final ResourceLocation artPigeon = mod("textures/entity/artillery_pigeon.png");

        // попугаи: разные виды (скин по варианту), каска по роли
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_SOLDIER.get(),
                manager -> new ParrotArmyRenderer(manager, null, helmetOlive, false));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_SHOOTER.get(),
                manager -> new ParrotArmyRenderer(manager, null, helmetBrown, false));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_MEDIC.get(),
                manager -> new ParrotArmyRenderer(manager, null, helmetWhite, false));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_CITIZEN.get(),
                manager -> new ParrotArmyRenderer(manager, null, null, false));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_TANK.get(),
                manager -> new VehicleRenderer(manager, false, tankParrot));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_ARTILLERY.get(),
                manager -> new VehicleRenderer(manager, true, artParrot));

        // голуби: серые, без хвоста и хохолка
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON.get(),
                manager -> new ParrotArmyRenderer(manager, pigeon, helmetDark, true));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON_BOMBER.get(),
                manager -> new ParrotArmyRenderer(manager, pigeonDark, helmetDark, true));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON_SNIPER.get(),
                manager -> new ParrotArmyRenderer(manager, pigeon, helmetDark, true));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON_CITIZEN.get(),
                manager -> new ParrotArmyRenderer(manager, pigeon, null, true));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON_TANK.get(),
                manager -> new VehicleRenderer(manager, false, tankPigeon));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON_ARTILLERY.get(),
                manager -> new VehicleRenderer(manager, true, artPigeon));
    }
}
