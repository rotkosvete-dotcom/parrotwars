package com.parrotwars.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.world.World;

/** Стрела стрелка: не бьёт своих и хозяина. */
public class SeedArrowEntity extends ArrowEntity {
    public SeedArrowEntity(World world, LivingEntity shooter) {
        super(world, shooter);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof ParrotSoldierEntity) return false;
        Entity shooter = this.getOwner();
        if (shooter instanceof ParrotSoldierEntity && target == ((ParrotSoldierEntity) shooter).getOwner()) {
            return false;
        }
        return super.canHitEntity(target);
    }
}
