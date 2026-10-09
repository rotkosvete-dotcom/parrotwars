package com.parrotwars.block;

import com.parrotwars.entity.ModEntities;
import com.parrotwars.entity.ParrotSoldierEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Казарма. ПКМ предметом в руке:
 *  5 семян пшеницы  -> попугай-солдат
 *  3 стрелы         -> попугай-стрелок
 *  1 золотая морковь -> попугай-медик
 */
public class BarracksBlock extends Block {

    public BarracksBlock(Properties props) {
        super(props);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                                BlockRayTraceResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        EntityType<? extends ParrotSoldierEntity> type = null;
        int cost = 0;
        if (stack.getItem() == Items.WHEAT_SEEDS) {
            type = ModEntities.PARROT_SOLDIER.get();
            cost = 5;
        } else if (stack.getItem() == Items.ARROW) {
            type = ModEntities.PARROT_SHOOTER.get();
            cost = 3;
        } else if (stack.getItem() == Items.GOLDEN_CARROT) {
            type = ModEntities.PARROT_MEDIC.get();
            cost = 1;
        }

        if (type == null) {
            if (!world.isClientSide) {
                player.displayClientMessage(new TranslationTextComponent("message.parrotwars.barracks_help"), true);
            }
            return ActionResultType.sidedSuccess(world.isClientSide);
        }

        if (!player.abilities.instabuild && stack.getCount() < cost) {
            if (!world.isClientSide) {
                player.displayClientMessage(new TranslationTextComponent("message.parrotwars.barracks_need", cost), true);
            }
            return ActionResultType.sidedSuccess(world.isClientSide);
        }

        if (!world.isClientSide) {
            if (!player.abilities.instabuild) {
                stack.shrink(cost);
            }
            ParrotSoldierEntity parrot = type.create(world);
            if (parrot != null) {
                parrot.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, world.random.nextFloat() * 360.0F, 0.0F);
                parrot.tame(player);
                world.addFreshEntity(parrot);
                world.playSound(null, pos, SoundEvents.PARROT_AMBIENT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
}
