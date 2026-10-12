package com.parrotwars.entity;

import net.minecraft.util.math.BlockPos;

/** Вражеский юнит, который умеет идти в атаку к заданной точке. */
public interface Marcher {
    void setMarchTarget(BlockPos pos);
}
