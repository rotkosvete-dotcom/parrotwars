package com.parrotwars.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/** Враг: танк голубей. */
public class PigeonTankEntity extends WarVehicleEntity implements EnemyUnit {
    public PigeonTankEntity(EntityType<? extends PigeonTankEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean isEnemyTeam() {
        return true;
    }

    @Override
    protected boolean isArtillery() {
        return false;
    }

    @Override
    protected int attackInterval() {
        return 40;
    }

    @Override
    protected float attackRange() {
        return 18F;
    }
}
