package com.parrotwars.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.parrotwars.ParrotWars;
import com.parrotwars.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Пока на игроке эффект "форма попугая", вместо него рисуется большой попугай. */
@Mod.EventBusSubscriber(modid = ParrotWars.MODID, value = Dist.CLIENT)
public class ParrotFormRender {
    private static final ResourceLocation TEX = new ResourceLocation("textures/entity/parrot/parrot_red_blue.png");
    private static ArmyParrotModel model;
    private static ParrotEntity dummy;

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        PlayerEntity player = event.getPlayer();
        if (!player.hasEffect(ModEffects.PARROT_FORM.get())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        event.setCanceled(true);

        if (model == null) {
            model = new ArmyParrotModel();
        }
        if (dummy == null || dummy.level != mc.level) {
            dummy = new ParrotEntity(EntityType.PARROT, mc.level);
        }
        dummy.setOnGround(player.isOnGround());

        float pt = event.getPartialRenderTick();
        float bodyYaw = MathHelper.rotLerp(pt, player.yBodyRotO, player.yBodyRot);
        float headYaw = MathHelper.rotLerp(pt, player.yHeadRotO, player.yHeadRot);
        float netHeadYaw = headYaw - bodyYaw;
        float pitch = MathHelper.lerp(pt, player.xRotO, player.xRot);
        float limbAmount = MathHelper.lerp(pt, player.animationSpeedOld, player.animationSpeed);
        float limbSwing = player.animationPosition - player.animationSpeed * (1.0F - pt);
        float age = player.tickCount + pt;
        float bob = player.isOnGround() ? 0.0F : (MathHelper.sin(age * 0.6F) + 1.0F) * 0.8F;

        model.setupAnim(dummy, limbSwing, limbAmount, bob, netHeadYaw, pitch);

        MatrixStack ms = event.getMatrixStack();
        ms.pushPose();
        ms.mulPose(Vector3f.YP.rotationDegrees(180.0F - bodyYaw));
        ms.scale(2.0F, 2.0F, 2.0F);
        ms.scale(-1.0F, -1.0F, 1.0F);
        ms.translate(0.0D, -1.501D, 0.0D);
        IVertexBuilder vb = event.getBuffers().getBuffer(RenderType.entityCutoutNoCull(TEX));
        model.renderToBuffer(ms, vb, event.getLight(), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        ms.popPose();
    }
}
