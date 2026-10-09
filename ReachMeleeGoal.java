package com.parrotwars.entity;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

/** Ближний бой с увеличенной дальностью: летающие птицы редко подлетают вплотную. */
public class ReachMeleeGoal extends MeleeAttackGoal {
    public ReachMeleeGoal(CreatureEntity mob, double speed, boolean longMemory) {
        super(mob, speed, longMemory);
    }

    @Override
    protected double getAttackReachSqr(LivingEntity target) {
        return 5.0D + target.getBbWidth();
    }
}
