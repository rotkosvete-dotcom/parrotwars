package com.parrotwars.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;

/** Кто свой, а кто чужой. */
public final class Teams {
    private Teams() {
    }

    /** true, если attacker не должен вредить victim (свои и хозяин). */
    public static boolean isFriendly(Entity attacker, Entity victim) {
        if (attacker == victim) return true;
        if (attacker instanceof EnemyUnit) return victim instanceof EnemyUnit;
        if (attacker instanceof ArmyUnit) {
            if (victim instanceof ArmyUnit) return true;
            if (attacker instanceof TameableEntity) {
                return victim == ((TameableEntity) attacker).getOwner();
            }
        }
        return false;
    }
}
