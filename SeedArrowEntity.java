package com.parrotwars.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** Стрела стрелка/снайпера: не бьёт своих. */
public class SeedArrowEntity extends ArrowEntity {
    public SeedArrowEntity(World world, LivingEntity shooter) {
        super(world, shooter);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        Entity shooter = this.getOwner();
        if (shooter != null && Teams.isFriendly(shooter, target)) {
            return false;
        }
        return super.canHitEntity(target);
    }

    /** Выстрел стрелой моба в цель. */
    public static void fire(MobEntity shooter, LivingEntity target, double damage, float inaccuracy) {
        SeedArrowEntity arrow = new SeedArrowEntity(shooter.level, shooter);
        double dx = target.getX() - shooter.getX();
        double dy = target.getY(0.33D) - arrow.getY();
        double dz = target.getZ() - shooter.getZ();
        double h = MathHelper.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + h * 0.2D, dz, 1.8F, inaccuracy);
        arrow.setBaseDamage(damage);
        arrow.pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
        shooter.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
        shooter.level.addFreshEntity(arrow);
    }
}
