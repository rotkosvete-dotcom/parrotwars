package com.parrotwars.item;

import com.parrotwars.entity.ParrotSoldierEntity;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Рация:
 *  ПКМ по блоку  - отряд идёт туда
 *  ПКМ по мобу   - отряд атакует цель
 *  ПКМ в воздух  - отряд следует за тобой
 *  Shift+ПКМ в воздух - отряд держит позицию
 */
public class RadioItem extends Item {
    private static final double RANGE = 64.0D;

    public RadioItem(Properties props) {
        super(props);
    }

    private static List<ParrotSoldierEntity> getSoldiers(PlayerEntity player) {
        return player.level.getEntitiesOfClass(ParrotSoldierEntity.class,
                player.getBoundingBox().inflate(RANGE), e -> e.isOwnedBy(player));
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        World world = ctx.getLevel();
        if (player == null) return ActionResultType.PASS;
        if (!world.isClientSide) {
            BlockPos pos = ctx.getClickedPos().above();
            List<ParrotSoldierEntity> list = getSoldiers(player);
            list.forEach(s -> s.orderMove(pos));
            player.displayClientMessage(new TranslationTextComponent("message.parrotwars.move", list.size()), true);
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            List<ParrotSoldierEntity> list = getSoldiers(player);
            if (player.isShiftKeyDown()) {
                list.forEach(ParrotSoldierEntity::orderHold);
                player.displayClientMessage(new TranslationTextComponent("message.parrotwars.hold", list.size()), true);
            } else {
                list.forEach(ParrotSoldierEntity::orderFollow);
                player.displayClientMessage(new TranslationTextComponent("message.parrotwars.follow", list.size()), true);
            }
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (target instanceof ParrotSoldierEntity && ((ParrotSoldierEntity) target).isOwnedBy(player)) {
            return ActionResultType.PASS;
        }
        if (!player.level.isClientSide) {
            List<ParrotSoldierEntity> list = getSoldiers(player);
            list.forEach(s -> s.orderAttack(target));
            player.displayClientMessage(new TranslationTextComponent("message.parrotwars.attack", list.size()), true);
        }
        return ActionResultType.sidedSuccess(player.level.isClientSide);
    }
}
