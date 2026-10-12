package com.parrotwars.entity;

import java.util.EnumSet;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

/** Враг: голубь-бомбардировщик. Зависает над целью и сбрасывает бомбы. */
public class PigeonBomberEntity extends PigeonEntity {

    public PigeonBomberEntity(EntityType<? extends PigeonBomberEntity> type, World world) {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute createBomberAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.FLYING_SPEED, 0.7D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new BombRunGoal(this));
        this.goalSelector.addGoal(2, new MarchGoal(this, () -> getMarchTarget(), () -> setMarchTarget(null)));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.addGoal(3, new ScanTargetGoal(this, Teams::isParrotSide, 28.0D));
    }

    public void dropBomb() {
        ShellEntity bomb = new ShellEntity(this.level, this, 9.0F, 3.0F, new ItemStack(Items.TNT));
        bomb.setPos(this.getX(), this.getY() - 0.3D, this.getZ());
        bomb.setDeltaMovement(0.0D, -0.2D, 0.0D);
        this.level.addFreshEntity(bomb);
    }

    static class BombRunGoal extends Goal {
        private final PigeonBomberEntity bomber;
        private int cooldown = 0;

        BombRunGoal(PigeonBomberEntity bomber) {
            this.bomber = bomber;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = bomber.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            LivingEntity t = bomber.getTarget();
            if (t == null) return;
            double dx = t.getX() - bomber.getX();
            double dz = t.getZ() - bomber.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (bomber.tickCount % 10 == 0 || bomber.getNavigation().isDone()) {
                bomber.getNavigation().moveTo(t.getX(), t.getY() + 5.0D, t.getZ(), 1.2D);
            }
            bomber.getLookControl().setLookAt(t, 30.0F, 30.0F);
            if (cooldown > 0) {
                cooldown--;
            } else if (horizontal < 4.0D && bomber.getY() > t.getY() + 1.5D) {
                bomber.dropBomb();
                cooldown = 50;
            }
        }
    }
}
