package com.parrotwars.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/** Артиллерия попугаев. */
public class ParrotArtilleryEntity extends WarVehicleEntity implements ArmyUnit {
    public ParrotArtilleryEntity(EntityType<? extends ParrotArtilleryEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean isEnemyTeam() {
        return false;
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
