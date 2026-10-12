package com.parrotwars.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;

/** Кто свой, а кто чужой. */
public final class Teams {
    private Teams() {
    }

    /** true, если attacker не должен вредить victim (свои, граждане своей стороны, хозяин). */
    public static boolean isFriendly(Entity attacker, Entity victim) {
        if (attacker == victim) return true;
        if (attacker instanceof EnemyUnit) {
            return victim instanceof EnemyUnit
                    || (victim instanceof CitizenEntity && ((CitizenEntity) victim).isPigeonSide());
        }
        if (attacker instanceof ArmyUnit) {
            if (victim instanceof ArmyUnit) return true;
            if (victim instanceof CitizenEntity && !((CitizenEntity) victim).isPigeonSide()) return true;
            if (attacker instanceof TameableEntity) {
                return victim == ((TameableEntity) attacker).getOwner();
            }
        }
        return false;
    }

    /** Цель для армии голубей: юниты попугаев и граждане-попугаи. */
    public static boolean isParrotSide(LivingEntity e) {
        return e instanceof ArmyUnit || (e instanceof CitizenEntity && !((CitizenEntity) e).isPigeonSide());
    }
}
