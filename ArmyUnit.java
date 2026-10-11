package com.parrotwars.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/** Юнит армии попугаев: слушается рацию. */
public interface ArmyUnit {
    boolean isOwnedByPlayer(PlayerEntity player);

    void orderFollow();

    void orderHold();

    void orderMove(BlockPos pos);

    void orderAttack(LivingEntity target);
}
