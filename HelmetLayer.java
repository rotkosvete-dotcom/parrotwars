package com.parrotwars.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.ResourceLocation;

/** Каска на голове попугая. */
public class HelmetLayer extends LayerRenderer<ParrotEntity, ArmyParrotModel> {
    private final ModelRenderer helmet;
    private final ResourceLocation texture;

    public HelmetLayer(IEntityRenderer<ParrotEntity, ArmyParrotModel> renderer, ResourceLocation texture) {
        super(renderer);
        this.texture = texture;
        this.helmet = new ModelRenderer(renderer.getModel(), 0, 0);
        this.helmet.setPos(0.0F, 0.0F, 0.0F);
        // купол каски
        this.helmet.texOffs(0, 0).addBox(-1.5F, -2.6F, -1.5F, 3.0F, 1.4F, 3.0F);
        // поля каски
        this.helmet.texOffs(0, 10).addBox(-1.8F, -1.6F, -1.8F, 3.6F, 0.5F, 3.6F);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, ParrotEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ModelRenderer head = this.getParentModel().getHeadPart();
        if (head == null || entity.isInvisible()) {
            return;
        }
        matrixStack.pushPose();
        head.translateAndRotate(matrixStack);
        IVertexBuilder vb = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.helmet.render(matrixStack, vb, packedLight, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
    }
}
