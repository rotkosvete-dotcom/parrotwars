package com.parrotwars.client;

import net.minecraft.client.renderer.entity.model.ParrotModel;
import net.minecraft.client.renderer.model.ModelRenderer;

/** Ванильная модель попугая + доступ к голове (чтобы надеть каску). */
public class ArmyParrotModel extends ParrotModel {
    private ModelRenderer headPart;

    public ArmyParrotModel() {
        super();
        // Голова: деталь по центру (x = 0) с самой маленькой высотой пивота (y).
        float bestY = Float.MAX_VALUE;
        for (ModelRenderer part : this.parts()) {
            if (Math.abs(part.x) < 0.01F && part.y < bestY) {
                bestY = part.y;
                headPart = part;
            }
        }
    }

    public ModelRenderer getHeadPart() {
        return headPart;
    }
}
