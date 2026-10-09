package com.parrotwars.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Враг: голубь-солдат. Нападает на игроков и приручённых попугаев. */
public class PigeonEntity extends ParrotEntity {

    public PigeonEntity(EntityType<? extends PigeonEntity> type, World world) {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute createPigeonAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FLYING_SPEED, 0.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, ParrotSoldierEntity.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return target.hurt(DamageSource.mobAttack(this), dmg);
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        return ActionResultType.PASS; // нельзя приручить или отравить печеньем
    }
}
