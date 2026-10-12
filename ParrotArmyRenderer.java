package com.parrotwars.client;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * Рендер на базе ванильной модели попугая.
 * fixedTexture == null -> скин зависит от вида попугая (5 видов).
 */
public class ParrotArmyRenderer extends MobRenderer<ParrotEntity, ArmyParrotModel> {
    public static final ResourceLocation[] SKINS = {
            new ResourceLocation("textures/entity/parrot/parrot_red_blue.png"),
            new ResourceLocation("textures/entity/parrot/parrot_blue.png"),
            new ResourceLocation("textures/entity/parrot/parrot_green.png"),
            new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png"),
            new ResourceLocation("textures/entity/parrot/parrot_grey.png")
    };

    private final ResourceLocation fixedTexture;

    public ParrotArmyRenderer(EntityRendererManager manager, ResourceLocation fixedTexture,
                              ResourceLocation helmetTexture, boolean pigeon) {
        super(manager, new ArmyParrotModel(pigeon), 0.3F);
        this.fixedTexture = fixedTexture;
        if (helmetTexture != null) {
            this.addLayer(new HelmetLayer(this, helmetTexture));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ParrotEntity entity) {
        if (fixedTexture != null) {
            return fixedTexture;
        }
        return SKINS[MathHelper.clamp(entity.getVariant(), 0, SKINS.length - 1)];
    }

    @Override
    public float getBob(ParrotEntity parrot, float partialTicks) {
        float f = MathHelper.lerp(partialTicks, parrot.oFlap, parrot.flap);
        float f1 = MathHelper.lerp(partialTicks, parrot.oFlapSpeed, parrot.flapSpeed);
        return (MathHelper.sin(f) + 1.0F) * f1;
    }
}
