package com.parrotwars.entity;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** Попугай-солдат. Приручается семенами пшеницы, слушается рацию. */
public class ParrotSoldierEntity extends ParrotEntity {

    public enum Order { FOLLOW, HOLD, MOVE }

    private Order order = Order.FOLLOW;
    private BlockPos moveTarget = null;

    public ParrotSoldierEntity(EntityType<? extends ParrotSoldierEntity> type, World world) {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute createSoldierAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, true));
        this.goalSelector.addGoal(2, new MoveOrderGoal(this));
        this.goalSelector.addGoal(3, new FollowOrderGoal(this));
        this.goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(6, new LookRandomlyGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<MobEntity>(this, MobEntity.class, 10, true, false,
                e -> e instanceof PigeonEntity) {
            @Override
            public boolean canUse() {
                return isTame() && super.canUse();
            }
        });
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return target.hurt(DamageSource.mobAttack(this), dmg);
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTame() && stack.getItem() == Items.WHEAT_SEEDS) {
            if (!this.level.isClientSide) {
                if (!player.abilities.instabuild) {
                    stack.shrink(1);
                }
                this.tame(player);
                this.level.broadcastEntityEvent(this, (byte) 7);
            }
            return ActionResultType.sidedSuccess(this.level.isClientSide);
        }
        return ActionResultType.PASS;
    }

    // ---------- Приказы ----------

    public void orderFollow() {
        this.order = Order.FOLLOW;
        this.moveTarget = null;
    }

    public void orderHold() {
        this.order = Order.HOLD;
        this.moveTarget = null;
        this.getNavigation().stop();
    }

    public void orderMove(BlockPos pos) {
        this.order = Order.MOVE;
        this.moveTarget = pos;
        this.setTarget(null);
    }

    public void orderAttack(LivingEntity target) {
        this.setTarget(target);
    }

    public Order getOrder() {
        return order;
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Order", order.ordinal());
        if (moveTarget != null) {
            tag.putBoolean("HasMove", true);
            tag.putInt("MoveX", moveTarget.getX());
            tag.putInt("MoveY", moveTarget.getY());
            tag.putInt("MoveZ", moveTarget.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        this.order = Order.values()[MathHelper.clamp(tag.getInt("Order"), 0, Order.values().length - 1)];
        if (tag.getBoolean("HasMove")) {
            this.moveTarget = new BlockPos(tag.getInt("MoveX"), tag.getInt("MoveY"), tag.getInt("MoveZ"));
        }
    }

    // ---------- Цели ИИ ----------

    static class MoveOrderGoal extends Goal {
        private final ParrotSoldierEntity mob;

        MoveOrderGoal(ParrotSoldierEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return mob.order == Order.MOVE && mob.moveTarget != null && mob.getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            BlockPos t = mob.moveTarget;
            if (t.closerThan(mob.blockPosition(), 2.5D)) {
                mob.orderHold();
                return;
            }
            if (mob.getNavigation().isDone()) {
                mob.getNavigation().moveTo(t.getX() + 0.5D, t.getY() + 0.5D, t.getZ() + 0.5D, 1.3D);
            }
        }
    }

    static class FollowOrderGoal extends FollowOwnerGoal {
        private final ParrotSoldierEntity soldier;

        FollowOrderGoal(ParrotSoldierEntity soldier) {
            super(soldier, 1.1D, 6.0F, 2.0F, true);
            this.soldier = soldier;
        }

        @Override
        public boolean canUse() {
            return soldier.order == Order.FOLLOW && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return soldier.order == Order.FOLLOW && super.canContinueToUse();
        }
    }
}
