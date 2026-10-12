package com.parrotwars.entity;

import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import com.parrotwars.country.CountryManager;

/**
 * Гражданин (попугай или голубь). Относится к стране правителя и имеет одобрение (0-100).
 * Довольные (>= 70) выходят на митинг в поддержку, недовольные (<= 30) - на митинг протеста.
 */
public abstract class CitizenEntity extends ParrotEntity {
    private UUID ruler = null;
    private int approval = 50;

    protected CitizenEntity(EntityType<? extends CitizenEntity> type, World world) {
        super(type, world);
    }

    /** true - гражданин голубиной республики. */
    public abstract boolean isPigeonSide();

    public static AttributeModifierMap.MutableAttribute createCitizenAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FLYING_SPEED, 0.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    public UUID getRuler() {
        return ruler;
    }

    public void setRuler(UUID ruler) {
        this.ruler = ruler;
    }

    public int getApproval() {
        return approval;
    }

    public void changeApproval(int delta) {
        this.approval = MathHelper.clamp(this.approval + delta, 0, 100);
    }

    /** 1 - поддержка, -1 - протест, 0 - спокоен. */
    public int mood() {
        if (approval >= 70) return 1;
        if (approval <= 30) return -1;
        return 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(2, new RallyGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 8.0F));
    }

    // ---------- Общение ----------

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean seeds = stack.getItem() == Items.WHEAT_SEEDS;
        boolean carrot = stack.getItem() == Items.GOLDEN_CARROT;
        if (!this.level.isClientSide) {
            if (seeds || carrot) {
                int gift = carrot ? 25 : 8;
                if (ruler == null) {
                    ruler = player.getUUID();
                }
                if (ruler.equals(player.getUUID())) {
                    if (!player.abilities.instabuild) stack.shrink(1);
                    changeApproval(gift);
                    heartBurst();
                } else if (approval <= 40 && carrot) {
                    // переманил на свою сторону
                    if (!player.abilities.instabuild) stack.shrink(1);
                    ruler = player.getUUID();
                    approval = 60;
                    heartBurst();
                    player.displayClientMessage(new TranslationTextComponent("message.parrotwars.citizen_defected"), true);
                    return ActionResultType.sidedSuccess(false);
                } else {
                    player.displayClientMessage(new TranslationTextComponent("message.parrotwars.citizen_loyal"), true);
                    return ActionResultType.sidedSuccess(false);
                }
            }
            player.displayClientMessage(new TranslationTextComponent("message.parrotwars.citizen_mood", approval), true);
        }
        return ActionResultType.sidedSuccess(this.level.isClientSide);
    }

    private void heartBurst() {
        if (this.level instanceof ServerWorld) {
            ((ServerWorld) this.level).sendParticles(ParticleTypes.HEART, getX(), getY() + 0.9D, getZ(),
                    5, 0.3D, 0.3D, 0.3D, 0.02D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level.isClientSide && source.getEntity() instanceof PlayerEntity) {
            changeApproval(-40);
        }
        return result;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!this.level.isClientSide && ruler != null) {
            final UUID r = ruler;
            for (CitizenEntity c : this.level.getEntitiesOfClass(CitizenEntity.class,
                    this.getBoundingBox().inflate(32.0D), e -> e != this && r.equals(e.getRuler()))) {
                c.changeApproval(-10);
            }
        }
    }

    // ---------- Жизнь ----------

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide || !(this.level instanceof ServerWorld)) {
            return;
        }
        ServerWorld world = (ServerWorld) this.level;
        if (this.tickCount % 200 == 0 && ruler != null) {
            CountryManager.Country c = CountryManager.get(world.getServer()).getCountry(ruler);
            if (c != null) {
                boolean inside = c.chunks.contains(new ChunkPos(this.blockPosition()).toLong());
                changeApproval(inside ? 2 : -2);
                for (long h : c.houses) {
                    if (BlockPos.of(h).closerThan(this.blockPosition(), 12.0D)) {
                        changeApproval(1);
                        break;
                    }
                }
            }
        }
        if (this.tickCount % 40 == 0 && mood() != 0 && this.getRandom().nextInt(3) == 0) {
            world.sendParticles(mood() > 0 ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ANGRY_VILLAGER,
                    getX(), getY() + 0.9D, getZ(), 1, 0.2D, 0.2D, 0.2D, 0.0D);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Approval", approval);
        if (ruler != null) {
            tag.putUUID("Ruler", ruler);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        approval = tag.contains("Approval") ? tag.getInt("Approval") : 50;
        ruler = tag.hasUUID("Ruler") ? tag.getUUID("Ruler") : null;
    }

    // ---------- Митинг ----------

    private BlockPos rallyPoint(int mood) {
        if (ruler == null || !(this.level instanceof ServerWorld)) {
            return null;
        }
        ServerWorld world = (ServerWorld) this.level;
        if (mood > 0) {
            ServerPlayerEntity p = world.getServer().getPlayerList().getPlayer(ruler);
            if (p != null && p.level == this.level && p.distanceToSqr(this) < 64.0D * 64.0D) {
                return p.blockPosition();
            }
        }
        CountryManager.Country c = CountryManager.get(world.getServer()).getCountry(ruler);
        if (c == null) {
            return null;
        }
        ChunkPos cp = new ChunkPos(c.capital);
        int x = (cp.x * 16 + 8);
        int z = (cp.z * 16 + 8);
        if (!world.hasChunkAt(new BlockPos(x, 64, z))) {
            return null;
        }
        return new BlockPos(x, world.getHeight(Heightmap.Type.WORLD_SURFACE, x, z), z);
    }

    private void rallyEffect() {
        boolean support = mood() > 0;
        if (!(this.level instanceof ServerWorld)) {
            return;
        }
        ServerWorld world = (ServerWorld) this.level;
        world.sendParticles(support ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER,
                getX(), getY() + 1.0D, getZ(), 2, 0.3D, 0.3D, 0.3D, 0.0D);
        if (this.getRandom().nextInt(4) == 0) {
            this.playSound(SoundEvents.PARROT_AMBIENT, 1.0F, support ? 1.5F : 0.6F);
        }
        final UUID r = ruler;
        int others = this.level.getEntitiesOfClass(CitizenEntity.class, this.getBoundingBox().inflate(8.0D),
                e -> e != this && r != null && r.equals(e.getRuler()) && e.mood() == mood()).size();
        if (others < 4 || r == null) {
            return;
        }
        ServerPlayerEntity p = world.getServer().getPlayerList().getPlayer(r);
        if (p == null) {
            return;
        }
        if (support) {
            p.addEffect(new EffectInstance(Effects.REGENERATION, 120, 0));
            p.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 120, 0));
        } else {
            p.addEffect(new EffectInstance(Effects.WEAKNESS, 120, 0));
        }
        long now = world.getGameTime();
        if (now - p.getPersistentData().getLong("pw_rally_msg") > 600L) {
            p.getPersistentData().putLong("pw_rally_msg", now);
            p.displayClientMessage(new TranslationTextComponent(
                    support ? "message.parrotwars.rally_support" : "message.parrotwars.rally_protest"), false);
        }
    }

    static class RallyGoal extends Goal {
        private final CitizenEntity citizen;
        private BlockPos point;
        private int ticks;

        RallyGoal(CitizenEntity citizen) {
            this.citizen = citizen;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (citizen.level.isClientSide || citizen.mood() == 0 || citizen.getRandom().nextInt(100) != 0) {
                return false;
            }
            point = citizen.rallyPoint(citizen.mood());
            return point != null;
        }

        @Override
        public boolean canContinueToUse() {
            return point != null && citizen.mood() != 0 && ticks < 600;
        }

        @Override
        public void start() {
            ticks = 0;
        }

        @Override
        public void tick() {
            ticks++;
            double px = point.getX() + 0.5D;
            double pz = point.getZ() + 0.5D;
            if (citizen.distanceToSqr(px, point.getY(), pz) > 36.0D) {
                if (citizen.getNavigation().isDone()) {
                    citizen.getNavigation().moveTo(px, point.getY() + 1.0D, pz, 1.0D);
                }
            } else {
                citizen.getNavigation().stop();
                if (ticks % 20 == 0) {
                    citizen.rallyEffect();
                }
            }
        }
    }
}
