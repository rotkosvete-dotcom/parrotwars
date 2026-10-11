package com.parrotwars.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/** Танк попугаев. */
public class ParrotTankEntity extends WarVehicleEntity implements ArmyUnit {
    public ParrotTankEntity(EntityType<? extends ParrotTankEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean isEnemyTeam() {
        return false;
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
