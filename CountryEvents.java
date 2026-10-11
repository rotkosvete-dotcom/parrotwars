package com.parrotwars.country;

import com.parrotwars.ParrotWars;
import com.parrotwars.entity.ArmyUnit;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Бонус страны: свои юниты лечатся на своей территории. */
@Mod.EventBusSubscriber(modid = ParrotWars.MODID)
public class CountryEvents {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isClientSide) return;
        if (!(event.world instanceof ServerWorld)) return;
        if (event.world.getGameTime() % 100 != 0) return;
        ServerWorld world = (ServerWorld) event.world;
        CountryManager manager = CountryManager.get(world.getServer());
        for (ServerPlayerEntity player : world.players()) {
            final ServerPlayerEntity owner = player;
            CountryManager.Country country = manager.getCountry(owner.getUUID());
            if (country == null) continue;
            List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(96.0D),
                    e -> e instanceof ArmyUnit && ((ArmyUnit) e).isOwnedByPlayer(owner) && e.getHealth() < e.getMaxHealth());
            for (LivingEntity e : list) {
                if (country.chunks.contains(new ChunkPos(e.blockPosition()).toLong())) {
                    e.heal(2.0F);
                }
            }
        }
    }
}
