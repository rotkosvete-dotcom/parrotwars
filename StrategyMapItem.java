package com.parrotwars.item;

import com.parrotwars.network.CountrySyncPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Карта командира: ПКМ - открыть стратегическую карту. */
public class StrategyMapItem extends Item {

    public StrategyMapItem(Properties props) {
        super(props);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide && player instanceof ServerPlayerEntity) {
            CountrySyncPacket.send((ServerPlayerEntity) player, true);
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }
}
