package com.parrotwars.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

/** Пока эффект активен, игрок превращён в попугая и умеет летать. */
public class ParrotFormEffect extends Effect {

    public ParrotFormEffect() {
        super(EffectType.NEUTRAL, 0xD62828);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity && !entity.level.isClientSide) {
            PlayerEntity player = (PlayerEntity) entity;
            if (!player.abilities.mayfly) {
                player.abilities.mayfly = true;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager manager, int amplifier) {
        super.removeAttributeModifiers(entity, manager, amplifier);
        if (entity instanceof PlayerEntity && !entity.level.isClientSide) {
            PlayerEntity player = (PlayerEntity) entity;
            if (!player.isCreative() && !player.isSpectator()) {
                player.abilities.mayfly = false;
                player.abilities.flying = false;
                player.onUpdateAbilities();
            }
        }
    }
}
