package com.parrotwars.client;

import com.parrotwars.entity.WarVehicleEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class VehicleRenderer extends MobRenderer<WarVehicleEntity, VehicleModel> {
    private final ResourceLocation texture;

    public VehicleRenderer(EntityRendererManager manager, boolean artillery, ResourceLocation texture) {
        super(manager, new VehicleModel(artillery), artillery ? 0.9F : 1.0F);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(WarVehicleEntity entity) {
        return texture;
    }
}
