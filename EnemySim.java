package com.parrotwars.country;

import com.parrotwars.entity.EnemyUnit;
import com.parrotwars.entity.Marcher;
import com.parrotwars.entity.ModEntities;
import com.parrotwars.entity.PigeonCitizenEntity;
import com.parrotwars.entity.CitizenEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

/** Жизнь голубиной республики: растёт армия, гарнизон у столицы, набеги на страну игрока. */
public class EnemySim {

    /** Вызывать раз в 100 тиков в Верхнем мире. */
    public static void tick(ServerWorld world, CountryManager manager) {
        CountryManager.Country enemy = manager.getCountry(CountryManager.ENEMY_ID);
        if (enemy == null) {
            return;
        }
        long time = world.getGameTime();
        if (time % 200 == 0 && enemy.strength < 40) {
            enemy.strength++;
            manager.setDirty();
        }
        if (time % 400 == 0) {
            garrison(world, enemy);
        }
        if (time >= enemy.nextRaid) {
            raid(world, manager, enemy);
        }
    }

    private static BlockPos spawnPoint(ServerWorld world, double cx, double cz, double spread) {
        int x = (int) Math.round(cx + (world.random.nextDouble() - 0.5D) * spread * 2.0D);
        int z = (int) Math.round(cz + (world.random.nextDouble() - 0.5D) * spread * 2.0D);
        int y = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }

    private static MobEntity spawn(ServerWorld world, EntityType<? extends MobEntity> type, BlockPos pos, boolean air) {
        MobEntity e = type.create(world);
        if (e == null) {
            return null;
        }
        e.moveTo(pos.getX() + 0.5D, pos.getY() + (air ? 3.0D : 0.0D), pos.getZ() + 0.5D,
                world.random.nextFloat() * 360.0F, 0.0F);
        world.addFreshEntity(e);
        return e;
    }

    private static void spawnArmyUnit(ServerWorld world, CountryManager.Country enemy, BlockPos pos, int roll, MobEntity[] out) {
        EntityType<? extends MobEntity> type;
        boolean air = true;
        if (roll < 45) {
            type = ModEntities.PIGEON.get();
        } else if (roll < 65) {
            type = ModEntities.PIGEON_SNIPER.get();
        } else if (roll < 80) {
            type = ModEntities.PIGEON_BOMBER.get();
        } else if (roll < 92 && enemy.strength >= 20) {
            type = ModEntities.PIGEON_TANK.get();
            air = false;
        } else if (roll >= 92 && enemy.strength >= 30) {
            type = ModEntities.PIGEON_ARTILLERY.get();
            air = false;
        } else {
            type = ModEntities.PIGEON.get();
        }
        out[0] = spawn(world, type, pos, air);
    }

    private static void garrison(ServerWorld world, CountryManager.Country enemy) {
        ChunkPos cp = new ChunkPos(enemy.capital);
        int cx = (cp.x * 16 + 8);
        int cz = (cp.z * 16 + 8);
        if (!world.hasChunkAt(new BlockPos(cx, 64, cz))) {
            return;
        }
        boolean near = false;
        for (ServerPlayerEntity p : world.players()) {
            double dx = p.getX() - cx;
            double dz = p.getZ() - cz;
            if (dx * dx + dz * dz < 100.0D * 100.0D) {
                near = true;
                break;
            }
        }
        if (!near) {
            return;
        }
        BlockPos center = new BlockPos(cx, world.getHeight(Heightmap.Type.WORLD_SURFACE, cx, cz), cz);
        AxisAlignedBB box = new AxisAlignedBB(center).inflate(48.0D);
        int military = world.getEntitiesOfClass(LivingEntity.class, box, e -> e instanceof EnemyUnit).size();
        int cap = 3 + enemy.strength / 6;
        if (military < cap) {
            MobEntity[] holder = new MobEntity[1];
            spawnArmyUnit(world, enemy, spawnPoint(world, cx, cz, 10.0D), world.random.nextInt(100), holder);
        }
        int civilians = world.getEntitiesOfClass(PigeonCitizenEntity.class, box).size();
        if (civilians < 3) {
            MobEntity c = spawn(world, ModEntities.PIGEON_CITIZEN.get(), spawnPoint(world, cx, cz, 8.0D), false);
            if (c instanceof CitizenEntity) {
                ((CitizenEntity) c).setRuler(CountryManager.ENEMY_ID);
            }
        }
    }

    private static void raid(ServerWorld world, CountryManager manager, CountryManager.Country enemy) {
        long time = world.getGameTime();
        if (enemy.strength < 12) {
            enemy.nextRaid = time + 1200L;
            return;
        }
        ChunkPos ec = new ChunkPos(enemy.capital);
        for (ServerPlayerEntity p : world.players()) {
            CountryManager.Country mine = manager.getCountry(p.getUUID());
            if (mine == null) {
                continue;
            }
            ChunkPos mc = new ChunkPos(mine.capital);
            int tx = (mc.x * 16 + 8);
            int tz = (mc.z * 16 + 8);
            double pdx = p.getX() - tx;
            double pdz = p.getZ() - tz;
            if (pdx * pdx + pdz * pdz > 160.0D * 160.0D) {
                continue;
            }
            double dirX = ec.x - mc.x;
            double dirZ = ec.z - mc.z;
            double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
            if (len < 0.001D) {
                dirX = 1.0D;
                dirZ = 0.0D;
                len = 1.0D;
            }
            int sx = (int) Math.round(tx + dirX / len * 48.0D);
            int sz = (int) Math.round(tz + dirZ / len * 48.0D);
            if (!world.hasChunkAt(new BlockPos(sx, 64, sz))) {
                continue;
            }
            BlockPos target = new BlockPos(tx, world.getHeight(Heightmap.Type.WORLD_SURFACE, tx, tz), tz);
            int size = Math.max(2, Math.min(10, enemy.strength / 3));
            MobEntity[] holder = new MobEntity[1];
            for (int i = 0; i < size; i++) {
                holder[0] = null;
                spawnArmyUnit(world, enemy, spawnPoint(world, sx, sz, 6.0D), world.random.nextInt(100), holder);
                if (holder[0] instanceof Marcher) {
                    ((Marcher) holder[0]).setMarchTarget(target);
                }
            }
            enemy.strength = Math.max(0, enemy.strength - size * 2);
            enemy.nextRaid = time + 6000L + world.random.nextInt(3000);
            manager.setDirty();
            p.displayClientMessage(new TranslationTextComponent("message.parrotwars.raid", size), false);
            return;
        }
        enemy.nextRaid = time + 1200L;
    }
}
