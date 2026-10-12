package com.parrotwars.client;

import net.minecraft.client.renderer.entity.model.ParrotModel;
import net.minecraft.client.renderer.model.ModelRenderer;

/** Ванильная модель попугая + доступ к голове (каска) + режим "голубь" (без хвоста). */
public class ArmyParrotModel extends ParrotModel {
    private ModelRenderer headPart;

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
        if (pigeon && tail != null && tail != headPart) {
            tail.visible = false;
        }
    }

    public ModelRenderer getHeadPart() {
        return headPart;
    }
}
