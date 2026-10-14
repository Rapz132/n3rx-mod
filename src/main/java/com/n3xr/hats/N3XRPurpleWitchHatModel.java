package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Purple Witch Hat: topi penyihir ungu bertingkat -- brim lebar dua lapis,
 * sabuk coklat dengan gesper biru muda, pita gelap, tingkat ungu dengan
 * strip terang, lalu ujung yang menekuk. Dibuat dari kode (tanpa Blockbench).
 *
 * Koordinat relatif ke pivot kepala: y = -8 puncak kepala, hat duduk di atasnya.
 *
 * Texture: textures/hats/purple_witch_hat.png, 64x64. Layout box UV:
 *   brim bawah  uv(0, 0)    14 x 1 x 14
 *   brim atas   uv(0, 15)   12 x 1 x 12
 *   sabuk       uv(0, 28)    8 x 2 x 8   (coklat)
 *   pita gelap  uv(32, 28)   7 x 2 x 7
 *   tingkat 3   uv(0, 38)    6 x 3 x 6   (ada strip terang)
 *   tingkat 4   uv(24, 38)   4 x 3 x 4
 *   ujung 1     uv(40, 38)   2 x 3 x 2   (child, dimiringkan)
 *   gesper      uv(48, 38)   2 x 2 x 1
 *   ujung 2     uv(54, 38)   1 x 2 x 1   (child dari ujung 1, dimiringkan lagi)
 */
public class N3XRPurpleWitchHatModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        ModelPartData hat = root.addChild(
                                "purple_witch_hat",
                                ModelPartBuilder.create()
                                        .uv(0, 0).cuboid(-7.0f, -9.0f, -7.0f, 14.0f, 1.0f, 14.0f)
                                        .uv(0, 15).cuboid(-6.0f, -10.0f, -6.0f, 12.0f, 1.0f, 12.0f)
                                        .uv(0, 28).cuboid(-4.0f, -12.0f, -4.0f, 8.0f, 2.0f, 8.0f)
                                        .uv(48, 38).cuboid(-1.0f, -12.0f, -5.0f, 2.0f, 2.0f, 1.0f)
                                        .uv(32, 28).cuboid(-3.5f, -14.0f, -3.5f, 7.0f, 2.0f, 7.0f)
                                        .uv(0, 38).cuboid(-3.0f, -17.0f, -3.0f, 6.0f, 3.0f, 6.0f)
                                        .uv(24, 38).cuboid(-2.0f, -20.0f, -2.0f, 4.0f, 3.0f, 4.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        // ujung topi menekuk ke samping (ubah roll kalau mau arah/derajat lain)
                        ModelPartData tip = hat.addChild(
                                "tip",
                                ModelPartBuilder.create()
                                        .uv(40, 38).cuboid(-1.0f, -3.0f, -1.0f, 2.0f, 3.0f, 2.0f),
                                ModelTransform.of(0.0f, -20.0f, 0.0f, 0.0f, 0.0f, 0.35f)
                        );

                        tip.addChild(
                                "tip2",
                                ModelPartBuilder.create()
                                        .uv(54, 38).cuboid(-0.5f, -2.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                                ModelTransform.of(0.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.5f)
                        );

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 64);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
