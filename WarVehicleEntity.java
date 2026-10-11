package com.parrotwars.entity;

import java.util.EnumSet;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Боевая машина: танк или артиллерия. Общая основа и для попугаев, и для голубей.
 * ArmyUnit реализуют только подклассы попугаев (чтобы рация не командовала вражескими).
 */
public abstract class WarVehicleEntity extends TameableEntity implements IRangedAttackMob {

    public enum Order { FOLLOW, HOLD, MOVE }

    private Order order = Order.FOLLOW;
    private BlockPos moveTarget = null;

    protected WarVehicleEntity(EntityType<? extends WarVehicleEntity> type, World world) {
        super(type, world);
        this.maxUpStep = 1.0F;
    }

    // Подклассы возвращают константы (вызывается из конструктора родителя).
    protected abstract boolean isEnemyTeam();

    protected abstract boolean isArtillery();

    protected abstract int attackInterval();

    protected abstract float attackRange();

    public static AttributeModifierMap.MutableAttribute createTankAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    public static AttributeModifierMap.MutableAttribute createArtilleryAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.14D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 0.9D, attackInterval(), attackRange()));
        if (isEnemyTeam()) {
            this.goalSelector.addGoal(3, new WaterAvoidingRandomWalkingGoal(this, 0.6D));
        } else {
            this.goalSelector.addGoal(2, new MoveOrderGoal(this));
            this.goalSelector.addGoal(3, new FollowOrderGoal(this));
        }
        this.goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(6, new LookRandomlyGoal(this));

        if (isEnemyTeam()) {
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
            this.targetSelector.addGoal(3, new ScanTargetGoal(this, e -> e instanceof ArmyUnit, 28.0D));
        } else {
            this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
            this.targetSelector.addGoal(3, new ScanTargetGoal(this, e -> e instanceof EnemyUnit, 28.0D));
        }
    }

    // ---------- Стрельба ----------

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dy = target.getY(0.5D) - this.getEyeY();
        double h = MathHelper.sqrt(dx * dx + dz * dz);
        ShellEntity shell;
        if (isArtillery()) {
            shell = new ShellEntity(this.level, this, 10.0F, 4.0F, new ItemStack(Items.FIRE_CHARGE));
            double hh = Math.max(h, 6.0D);
            double speed = Math.sqrt(0.03D * hh) * 1.18D;
            double hx = h > 0.01D ? dx / h : 0.0D;
            double hz = h > 0.01D ? dz / h : 0.0D;
            shell.shoot(hx * 0.7071D, 0.7071D, hz * 0.7071D, (float) speed, 0.5F);
        } else {
            shell = new ShellEntity(this.level, this, 7.0F, 2.5F, new ItemStack(Items.FIRE_CHARGE));
            shell.shoot(dx, dy + h * 0.05D, dz, 2.0F, 1.0F);
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE, 0.8F, 1.6F);
        this.level.addFreshEntity(shell);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level.isClientSide) {
            Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity && !Teams.isFriendly(this, attacker)) {
                this.setTarget((LivingEntity) attacker);
            }
        }
        return result;
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isEnemyTeam() && !this.isTame() && stack.getItem() == Items.WHEAT_SEEDS) {
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

    @Override
    public AgeableEntity getBreedOffspring(ServerWorld world, AgeableEntity mate) {
        return null;
    }

    // ---------- Приказы ----------

    public boolean isOwnedByPlayer(PlayerEntity player) {
        return this.isOwnedBy(player);
    }

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
        private final WarVehicleEntity mob;

        MoveOrderGoal(WarVehicleEntity mob) {
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
            if (t.closerThan(mob.blockPosition(), 3.0D)) {
                mob.orderHold();
                return;
            }
            if (mob.getNavigation().isDone()) {
                mob.getNavigation().moveTo(t.getX() + 0.5D, t.getY(), t.getZ() + 0.5D, 1.0D);
            }
        }
    }

    static class FollowOrderGoal extends FollowOwnerGoal {
        private final WarVehicleEntity vehicle;

        FollowOrderGoal(WarVehicleEntity vehicle) {
            super(vehicle, 1.0D, 8.0F, 3.0F, false);
            this.vehicle = vehicle;
        }

        @Override
        public boolean canUse() {
            return vehicle.order == Order.FOLLOW && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return vehicle.order == Order.FOLLOW && super.canContinueToUse();
        }
    }
}
