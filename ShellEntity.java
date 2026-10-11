package com.parrotwars.entity;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/** Снаряд танка, артиллерии и бомба. Взрывается с уроном по площади, блоки не ломает, своих не бьёт. */
public class ShellEntity extends SnowballEntity {
    private final float damage;
    private final float radius;

    public ShellEntity(World world, LivingEntity shooter, float damage, float radius, ItemStack visual) {
        super(world, shooter);
        this.damage = damage;
        this.radius = radius;
        this.setItem(visual);
    }

    private static boolean canDamage(Entity owner, LivingEntity target) {
        if (owner == null) return true;
        if (target == owner) return false;
        if (Teams.isFriendly(owner, target)) return false;
        if (owner instanceof ArmyUnit) {
            return target instanceof EnemyUnit || target instanceof IMob;
        }
        return true;
    }

    @Override
    protected void onHit(RayTraceResult result) {
        if (this.level.isClientSide) {
            return;
        }
        final Entity owner = this.getOwner();
        List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius), e -> e.isAlive() && canDamage(owner, e));
        for (LivingEntity e : list) {
            double d = Math.sqrt(e.distanceToSqr(this));
            if (d <= radius) {
                float f = damage * (1.0F - (float) (d / radius) * 0.5F);
                e.hurt(DamageSource.thrown(this, owner), f);
            }
        }
        if (this.level instanceof ServerWorld) {
            ((ServerWorld) this.level).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE,
                SoundCategory.NEUTRAL, 1.5F, 1.2F);
        this.remove();
    }
}
