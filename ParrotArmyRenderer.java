package com.parrotwars.client;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/** Рендер на базе ванильной модели попугая: своя текстура и (по желанию) каска. */
public class ParrotArmyRenderer extends MobRenderer<ParrotEntity, ArmyParrotModel> {
    private final ResourceLocation texture;

    public ParrotArmyRenderer(EntityRendererManager manager, ResourceLocation texture, ResourceLocation helmetTexture) {
        super(manager, new ArmyParrotModel(), 0.3F);
        this.texture = texture;
        if (helmetTexture != null) {
            this.addLayer(new HelmetLayer(this, helmetTexture));
        }
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
