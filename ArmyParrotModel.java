package com.parrotwars.client;

import net.minecraft.client.renderer.entity.model.ParrotModel;
import net.minecraft.client.renderer.model.ModelRenderer;

/** Ванильная модель попугая + доступ к голове (каска) + режим "голубь" (без хвоста). */
public class ArmyParrotModel extends ParrotModel {
    private ModelRenderer headPart;
    private ModelRenderer tailPart;
    private final java.util.List<ModelRenderer> legParts = new java.util.ArrayList<>();

    public ArmyParrotModel() {
        this(false);
    }

    public ArmyParrotModel(boolean pigeon) {
        super();
        // Голова: деталь по центру (x = 0) с самым маленьким y пивота.
        // Хвост: деталь по центру с самым большим y пивота.
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        ModelRenderer tail = null;
        for (ModelRenderer part : this.parts()) {
            if (Math.abs(part.x) < 0.01F) {
                if (part.y < minY) {
                    minY = part.y;
                    headPart = part;
                }
                if (part.y > maxY) {
                    maxY = part.y;
                    tail = part;
                }
            }
        }
        if (tail != null && tail != headPart) {
            tailPart = tail;
            if (pigeon) {
                tail.visible = false;
            }
        }
        // Лапы: пара деталей по бокам (|x| около 1) внизу модели (y > 20).
        for (ModelRenderer part : this.parts()) {
            if (Math.abs(Math.abs(part.x) - 1.0F) < 0.25F && part.y > 20.0F) {
                legParts.add(part);
            }
        }
    }

    /** Вернуть лапы и хвост в обычное положение (против "разъезжающихся" лап при ходьбе игрока). */
    public void stabilize() {
        if (tailPart != null) {
            tailPart.xRot = 1.015F;
            tailPart.yRot = 0.0F;
            tailPart.zRot = 0.0F;
        }
        for (ModelRenderer leg : legParts) {
            leg.xRot = -0.0299F;
            leg.yRot = 0.0F;
            leg.zRot = 0.0F;
        }
    }

    public ModelRenderer getHeadPart() {
        return headPart;
    }
}
