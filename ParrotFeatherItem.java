package com.parrotwars.item;

import com.parrotwars.effect.ModEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/** Перо попугая: ПКМ - превратиться в попугая (полёт) и обратно. */
public class ParrotFeatherItem extends Item {

    public ParrotFeatherItem(Properties props) {
        super(props);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            Effect effect = ModEffects.PARROT_FORM.get();
            if (player.hasEffect(effect)) {
                player.removeEffect(effect);
                player.displayClientMessage(new TranslationTextComponent("message.parrotwars.parrot_off"), true);
            } else {
                player.addEffect(new EffectInstance(effect, 20 * 60 * 60 * 10, 0, false, false));
                player.displayClientMessage(new TranslationTextComponent("message.parrotwars.parrot_on"), true);
            }
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }
}
