package com.parrotwars.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.parrotwars.entity.WarVehicleEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

/** Простая модель танка или артиллерии (башня крутится за взглядом, ствол за наклоном). */
public class VehicleModel extends EntityModel<WarVehicleEntity> {
    private static final float RAD = 0.017453292F;

    private final boolean artillery;
    private final ModelRenderer body;
    private final ModelRenderer turret;
    private final ModelRenderer barrel;

    public VehicleModel(boolean artillery) {
        this.artillery = artillery;
        this.texWidth = 128;
        this.texHeight = 128;

        body = new ModelRenderer(this);
        body.setPos(0.0F, 24.0F, 0.0F);
        turret = new ModelRenderer(this);
        barrel = new ModelRenderer(this);

        if (!artillery) {
            body.texOffs(0, 0).addBox(-10.0F, -9.0F, -14.0F, 20.0F, 5.0F, 28.0F);   // корпус
            body.texOffs(0, 34).addBox(-12.0F, -6.0F, -14.0F, 4.0F, 6.0F, 28.0F);   // левая гусеница
            body.texOffs(64, 34).addBox(8.0F, -6.0F, -14.0F, 4.0F, 6.0F, 28.0F);    // правая гусеница

            turret.setPos(0.0F, -9.0F, 0.0F);
            turret.texOffs(0, 70).addBox(-6.0F, -6.0F, -7.0F, 12.0F, 6.0F, 14.0F);  // башня
            turret.texOffs(0, 92).addBox(-2.0F, -10.0F, 1.0F, 4.0F, 4.0F, 4.0F);    // голова попугая
            turret.texOffs(16, 92).addBox(-1.0F, -8.5F, -1.0F, 2.0F, 1.0F, 2.0F);   // клюв

            barrel.setPos(0.0F, -3.0F, -7.0F);
            barrel.texOffs(54, 70).addBox(-1.5F, -1.5F, -18.0F, 3.0F, 3.0F, 18.0F); // ствол
        } else {
            body.texOffs(0, 0).addBox(-8.0F, -6.0F, -9.0F, 16.0F, 3.0F, 18.0F);     // лафет
            body.texOffs(0, 22).addBox(-11.0F, -8.0F, -3.0F, 3.0F, 8.0F, 6.0F);     // левое колесо
            body.texOffs(20, 22).addBox(8.0F, -8.0F, -3.0F, 3.0F, 8.0F, 6.0F);      // правое колесо
            body.texOffs(40, 22).addBox(-8.0F, -4.0F, -1.0F, 16.0F, 2.0F, 2.0F);    // ось

            turret.setPos(0.0F, -6.0F, 0.0F);
            turret.texOffs(0, 38).addBox(-9.0F, -12.0F, -4.0F, 18.0F, 10.0F, 2.0F); // щит
            turret.texOffs(0, 52).addBox(-2.0F, -4.0F, 3.0F, 4.0F, 4.0F, 4.0F);     // голова попугая
            turret.texOffs(16, 52).addBox(-1.0F, -2.5F, 1.0F, 2.0F, 1.0F, 2.0F);    // клюв

            barrel.setPos(0.0F, -5.0F, -3.0F);
            barrel.texOffs(42, 38).addBox(-2.0F, -2.0F, -24.0F, 4.0F, 4.0F, 26.0F); // ствол
        }
        turret.addChild(barrel);
        body.addChild(turret);
    }

    @Override
    public void setupAnim(WarVehicleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        turret.yRot = netHeadYaw * RAD;
        barrel.xRot = artillery ? -0.5F : headPitch * RAD;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        body.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
