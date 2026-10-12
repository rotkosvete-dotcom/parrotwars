package com.parrotwars.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/** Гражданин-попугай. */
public class ParrotCitizenEntity extends CitizenEntity {
    public ParrotCitizenEntity(EntityType<? extends ParrotCitizenEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean isPigeonSide() {
        return false;
    }
}
