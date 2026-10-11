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

    // Ванильные текстуры попугаев
    private static final ResourceLocation SOLDIER_TEX = new ResourceLocation("textures/entity/parrot/parrot_red_blue.png");
    private static final ResourceLocation SHOOTER_TEX = new ResourceLocation("textures/entity/parrot/parrot_green.png");
    private static final ResourceLocation MEDIC_TEX = new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png");
    private static final ResourceLocation PIGEON_TEX = new ResourceLocation("textures/entity/parrot/parrot_grey.png");
    private static final ResourceLocation BOMBER_TEX = new ResourceLocation("textures/entity/parrot/parrot_blue.png");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        final ResourceLocation helmetOlive = mod("textures/entity/helmet_olive.png");
        final ResourceLocation helmetWhite = mod("textures/entity/helmet_white.png");
        final ResourceLocation helmetDark = mod("textures/entity/helmet_dark.png");
        final ResourceLocation tankParrot = mod("textures/entity/tank_parrot.png");
        final ResourceLocation tankPigeon = mod("textures/entity/tank_pigeon.png");
        final ResourceLocation artParrot = mod("textures/entity/artillery_parrot.png");
        final ResourceLocation artPigeon = mod("textures/entity/artillery_pigeon.png");

        // попугаи
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_SOLDIER.get(),
                manager -> new ParrotArmyRenderer(manager, SOLDIER_TEX, helmetOlive));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_SHOOTER.get(),
                manager -> new ParrotArmyRenderer(manager, SHOOTER_TEX, helmetOlive));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_MEDIC.get(),
                manager -> new ParrotArmyRenderer(manager, MEDIC_TEX, helmetWhite));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_TANK.get(),
                manager -> new VehicleRenderer(manager, false, tankParrot));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_ARTILLERY.get(),
                manager -> new VehicleRenderer(manager, true, artParrot));

        // голуби
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON.get(),
                manager -> new ParrotArmyRenderer(manager, PIGEON_TEX, helmetDark));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON_BOMBER.get(),
                manager -> new ParrotArmyRenderer(manager, BOMBER_TEX, helmetDark));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON_SNIPER.get(),
                manager -> new ParrotArmyRenderer(manager, PIGEON_TEX, helmetDark));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON_TANK.get(),
                manager -> new VehicleRenderer(manager, false, tankPigeon));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON_ARTILLERY.get(),
                manager -> new VehicleRenderer(manager, true, artPigeon));
    }
}
