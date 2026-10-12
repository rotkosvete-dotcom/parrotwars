package com.parrotwars.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/** Гражданин-голубь (голубиная республика). */
public class PigeonCitizenEntity extends CitizenEntity {
    public PigeonCitizenEntity(EntityType<? extends PigeonCitizenEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean isPigeonSide() {
        return true;
    }
}
