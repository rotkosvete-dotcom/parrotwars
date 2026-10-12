package com.parrotwars.country;

import com.parrotwars.ParrotWars;
import com.parrotwars.effect.ModEffects;
import com.parrotwars.entity.ArmyUnit;
import com.parrotwars.entity.CitizenEntity;
import com.parrotwars.entity.ModEntities;
import com.parrotwars.entity.ParrotCitizenEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Жизнь стран: лечение на своей земле, отдых в домах, граждане из домов, вражеская республика. */
@Mod.EventBusSubscriber(modid = ParrotWars.MODID)
public class CountryEvents {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isClientSide) return;
        if (!(event.world instanceof ServerWorld)) return;
        long time = event.world.getGameTime();
        if (time % 100 != 0) return;
        ServerWorld world = (ServerWorld) event.world;
        CountryManager manager = CountryManager.get(world.getServer());

        for (ServerPlayerEntity player : world.players()) {
            final ServerPlayerEntity owner = player;
            CountryManager.Country country = manager.getCountry(owner.getUUID());
            if (country == null) continue;
            long chunk = new ChunkPos(owner.blockPosition()).toLong();

            // лечение своих юнитов на своей земле
            List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(96.0D),
                    e -> e instanceof ArmyUnit && ((ArmyUnit) e).isOwnedByPlayer(owner) && e.getHealth() < e.getMaxHealth());
            for (LivingEntity e : list) {
                if (country.chunks.contains(new ChunkPos(e.blockPosition()).toLong())) {
                    e.heal(2.0F);
                }
            }

            // отдых в доме: в форме попугая, под крышей, на своей земле
            if (owner.hasEffect(ModEffects.PARROT_FORM.get()) && country.chunks.contains(chunk)
                    && !world.canSeeSky(owner.blockPosition())) {
                owner.addEffect(new EffectInstance(Effects.REGENERATION, 140, 0, true, false));
            }
        }

        // граждане появляются в домах (по 2 на дом)
        if (time % 600 == 0) {
            for (Map.Entry<UUID, CountryManager.Country> e : manager.all().entrySet()) {
                if (e.getValue().npc) continue;
                for (long h : e.getValue().houses) {
                    BlockPos pos = BlockPos.of(h);
                    if (!world.hasChunkAt(pos)) continue;
                    final UUID rulerId = e.getKey();
                    int count = world.getEntitiesOfClass(CitizenEntity.class, new AxisAlignedBB(pos).inflate(20.0D),
                            c -> rulerId.equals(c.getRuler())).size();
                    if (count < 2) {
                        ParrotCitizenEntity citizen = ModEntities.PARROT_CITIZEN.get().create(world);
                        if (citizen != null) {
                            citizen.moveTo(pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D,
                                    world.random.nextFloat() * 360.0F, 0.0F);
                            citizen.setVariant(world.random.nextInt(5));
                            citizen.setRuler(rulerId);
                            world.addFreshEntity(citizen);
                        }
                    }
                }
            }
        }

        // вражеская республика живёт в Верхнем мире
        if (world.dimension() == World.OVERWORLD) {
            EnemySim.tick(world, manager);
        }
    }
}
