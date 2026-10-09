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
    // Пока используем ванильные текстуры попугаев. Позже заменим на свои.
    private static final ResourceLocation SOLDIER_TEX = new ResourceLocation("textures/entity/parrot/parrot_red_blue.png");
    private static final ResourceLocation SHOOTER_TEX = new ResourceLocation("textures/entity/parrot/parrot_green.png");
    private static final ResourceLocation MEDIC_TEX = new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png");
    private static final ResourceLocation HELMET_OLIVE = new ResourceLocation(ParrotWars.MODID, "textures/entity/helmet_olive.png");
    private static final ResourceLocation HELMET_WHITE = new ResourceLocation(ParrotWars.MODID, "textures/entity/helmet_white.png");
    private static final ResourceLocation PIGEON_TEX = new ResourceLocation("textures/entity/parrot/parrot_grey.png");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_SOLDIER.get(),
                manager -> new ParrotArmyRenderer(manager, SOLDIER_TEX, HELMET_OLIVE));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_SHOOTER.get(),
                manager -> new ParrotArmyRenderer(manager, SHOOTER_TEX, HELMET_OLIVE));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PARROT_MEDIC.get(),
                manager -> new ParrotArmyRenderer(manager, MEDIC_TEX, HELMET_WHITE));
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIGEON.get(),
                manager -> new ParrotArmyRenderer(manager, PIGEON_TEX, null));
    }
}
