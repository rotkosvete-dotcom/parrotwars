package com.parrotwars.effect;

import com.parrotwars.ParrotWars;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** В форме попугая игрок становится маленьким (как настоящий попугай). */
@Mod.EventBusSubscriber(modid = ParrotWars.MODID)
public class ParrotFormEvents {

    @SubscribeEvent
    public static void onSize(EntityEvent.Size event) {
        Entity e = event.getEntity();
        // Событие приходит и из конструктора игрока, когда список эффектов ещё не создан - пропускаем.
        if (e == null || e.tickCount == 0) {
            return;
        }
        if (e instanceof PlayerEntity && ((PlayerEntity) e).hasEffect(ModEffects.PARROT_FORM.get())) {
            event.setNewSize(EntitySize.scalable(0.5F, 0.9F));
            event.setNewEyeHeight(0.7F);
        }
    }

    /** Когда эффект включился/выключился - пересчитать размер. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        PlayerEntity player = event.player;
        boolean form = player.hasEffect(ModEffects.PARROT_FORM.get());
        boolean small = player.getBbWidth() < 0.55F;
        if (form != small) {
            player.refreshDimensions();
        }
    }
}
