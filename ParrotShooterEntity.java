package com.parrotwars.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** Попугай-стрелок: бьёт издалека. */
public class ParrotShooterEntity extends ParrotSoldierEntity implements IRangedAttackMob {

    public ParrotShooterEntity(EntityType<? extends ParrotShooterEntity> type, World world) {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute createShooterAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    protected void registerAttackGoal() {
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.1D, 20, 14.0F));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        SeedArrowEntity.fire(this, target, 2.0D, 6.0F);
    }
}
