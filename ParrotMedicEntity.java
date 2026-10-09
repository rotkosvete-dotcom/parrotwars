package com.parrotwars.entity;

import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/** Попугай-медик: не дерётся, лечит хозяина и союзных попугаев вокруг. */
public class ParrotMedicEntity extends ParrotSoldierEntity {

    public ParrotMedicEntity(EntityType<? extends ParrotMedicEntity> type, World world) {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute createMedicAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void registerAttackGoal() {
        // медик не атакует
    }

    @Override
    protected void registerTargetGoals() {
        // и не выбирает цели
    }

    @Override
    protected boolean canFight() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide && this.isTame() && this.tickCount % 40 == 0) {
            healAllies();
        }
    }

    private void healAllies() {
        final LivingEntity owner = this.getOwner();
        if (owner == null) return;
        List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D),
                e -> e.isAlive() && e.getHealth() < e.getMaxHealth()
                        && (e == owner || (e instanceof ParrotSoldierEntity && ((ParrotSoldierEntity) e).isOwnedBy(owner))));
        for (LivingEntity e : list) {
            e.heal(2.0F);
            if (this.level instanceof ServerWorld) {
                ((ServerWorld) this.level).sendParticles(ParticleTypes.HEART, e.getX(), e.getY(0.8D), e.getZ(),
                        3, 0.3D, 0.3D, 0.3D, 0.02D);
            }
        }
    }
}
