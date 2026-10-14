package com.n3xr.hats;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Top Hat: topi tinggi hitam dengan pita merah, dibuat langsung dari kode
 * (3 kotak) tanpa file model dari Blockbench.
 *
 * Koordinat relatif ke pivot kepala (konvensi sama dengan hat lain):
 * y = -8 puncak kepala, y = 0 leher. Hat duduk di atas kepala (y < -8).
 *
 * Texture: textures/hats/top_hat.png, 64x32. Layout box UV:
 *   brim  uv(0, 0)   12 x 1 x 12   pinggiran topi
 *   crown uv(0, 13)   8 x 7 x 8    badan topi
 *   band  uv(32, 13)  8 x 2 x 8    pita merah (di-dilate 0.25 supaya tidak z-fight)
 */
public class N3XRTopHatModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        root.addChild(
                                "top_hat",
                                ModelPartBuilder.create()
                                        .uv(0, 0).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f)
                                        .uv(0, 13).cuboid(-4.0f, -16.0f, -4.0f, 8.0f, 7.0f, 8.0f)
                                        .uv(32, 13).cuboid(-4.0f, -11.0f, -4.0f, 8.0f, 2.0f, 8.0f, new Dilation(0.25f)),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 32);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
