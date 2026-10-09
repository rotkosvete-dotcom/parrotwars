package com.parrotwars.client;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.ParrotModel;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/** Рендер на базе ванильной модели попугая, с другой текстурой. */
public class ParrotArmyRenderer extends MobRenderer<ParrotEntity, ParrotModel> {
    private final ResourceLocation texture;

    public ParrotArmyRenderer(EntityRendererManager manager, ResourceLocation texture) {
        super(manager, new ParrotModel(), 0.3F);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(ParrotEntity entity) {
        return texture;
    }

    @Override
    public float getBob(ParrotEntity parrot, float partialTicks) {
        float f = MathHelper.lerp(partialTicks, parrot.oFlap, parrot.flap);
        float f1 = MathHelper.lerp(partialTicks, parrot.oFlapSpeed, parrot.flapSpeed);
        return (MathHelper.sin(f) + 1.0F) * f1;
    }
}
