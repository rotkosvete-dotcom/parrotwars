package com.parrotwars.entity;

import java.util.EnumSet;
import java.util.function.Supplier;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

/** Идёт к точке атаки, пока нет цели; дойдя - останавливается. */
public class MarchGoal extends Goal {
    private final MobEntity mob;
    private final Supplier<BlockPos> getter;
    private final Runnable clearer;

    public MarchGoal(MobEntity mob, Supplier<BlockPos> getter, Runnable clearer) {
        this.mob = mob;
        this.getter = getter;
        this.clearer = clearer;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return getter.get() != null && mob.getTarget() == null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        BlockPos t = getter.get();
        if (t == null) {
            return;
        }
        if (t.closerThan(mob.blockPosition(), 10.0D)) {
            clearer.run();
            mob.getNavigation().stop();
            return;
        }
        if (mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(t.getX() + 0.5D, t.getY(), t.getZ() + 0.5D, 1.0D);
        }
    }
}
