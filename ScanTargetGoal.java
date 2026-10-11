package com.parrotwars.entity;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

/** Сам ищет ближайшую подходящую цель в радиусе и выбирает её. */
public class ScanTargetGoal extends Goal {
    private final MobEntity mob;
    private final Predicate<LivingEntity> filter;
    private final double radius;
    private LivingEntity found;

    public ScanTargetGoal(MobEntity mob, Predicate<LivingEntity> filter, double radius) {
        this.mob = mob;
        this.filter = filter;
        this.radius = radius;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity current = mob.getTarget();
        if (current != null && current.isAlive()) return false;
        if (mob.getRandom().nextInt(10) != 0) return false;
        List<LivingEntity> list = mob.level.getEntitiesOfClass(LivingEntity.class,
                mob.getBoundingBox().inflate(radius, radius / 2.0D, radius),
                e -> e != mob && e.isAlive() && filter.test(e));
        found = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : list) {
            double d = mob.distanceToSqr(e);
            if (d < best) {
                best = d;
                found = e;
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
        mob.setTarget(found);
    }
}
