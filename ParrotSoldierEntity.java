package com.parrotwars.entity;

import java.util.EnumSet;
import java.util.List;
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

    // ---------- ИИ (подклассы переопределяют хуки) ----------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        registerAttackGoal();
        this.goalSelector.addGoal(2, new MoveOrderGoal(this));
        this.goalSelector.addGoal(3, new FollowOrderGoal(this));
        this.goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(6, new LookRandomlyGoal(this));
        registerTargetGoals();
    }

    protected void registerAttackGoal() {
        this.goalSelector.addGoal(1, new ReachMeleeGoal(this, 1.3D, true));
    }

    protected void registerTargetGoals() {
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HuntPigeonsGoal(this));
    }

    /** Вступает ли юнит в бой (медик - нет). */
    protected boolean canFight() {
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return target.hurt(DamageSource.mobAttack(this), dmg);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level.isClientSide && canFight()) {
            Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity && !isOwnedBy((LivingEntity) attacker)
                    && !(attacker instanceof ParrotSoldierEntity)) {
                this.setTarget((LivingEntity) attacker);
            }
        }
        return result;
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

    /** Сам ищет ближайшего голубя в радиусе и нападает. */
    static class HuntPigeonsGoal extends Goal {
        private final ParrotSoldierEntity soldier;
        private PigeonEntity found;

        HuntPigeonsGoal(ParrotSoldierEntity soldier) {
            this.soldier = soldier;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity current = soldier.getTarget();
            if (current != null && current.isAlive()) return false;
            if (soldier.getRandom().nextInt(10) != 0) return false;
            List<PigeonEntity> list = soldier.level.getEntitiesOfClass(PigeonEntity.class,
                    soldier.getBoundingBox().inflate(20.0D, 10.0D, 20.0D), e -> e.isAlive());
            found = null;
            double best = Double.MAX_VALUE;
            for (PigeonEntity p : list) {
                double d = soldier.distanceToSqr(p);
                if (d < best) {
                    best = d;
                    found = p;
                }
            }
            return found != null;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            soldier.setTarget(found);
        }
    }

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
