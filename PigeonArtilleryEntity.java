package com.parrotwars.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/** Враг: артиллерия голубей. */
public class PigeonArtilleryEntity extends WarVehicleEntity implements EnemyUnit, Marcher {
    public PigeonArtilleryEntity(EntityType<? extends PigeonArtilleryEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean isEnemyTeam() {
        return true;
    }

    @Override
    protected boolean isArtillery() {
        return true;
    }

    @Override
    protected int attackInterval() {
        return 90;
    }

    @Override
    protected float attackRange() {
        return 36F;
    }
}
