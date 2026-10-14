package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Purple Witch Hat: topi penyihir ungu bertingkat -- brim lebar dua lapis,
 * sabuk coklat dengan gesper emas bermata biru, pita gelap, tiga tingkat
 * ungu yang makin mengecil, lalu ujung yang menekuk KE BELAKANG.
 * Dibuat dari kode (tanpa Blockbench).
 *
 * Koordinat relatif ke pivot kepala: y = -8 puncak kepala, hat duduk di atasnya.
 * Depan kepala = -z, belakang = +z.
 *
 * Texture: textures/hats/purple_witch_hat.png, 64x64. Layout box UV:
 *   brim bawah  uv(0, 0)    14 x 1 x 14
 *   brim atas   uv(0, 15)   12 x 1 x 12
 *   sabuk       uv(0, 28)    8 x 2 x 8   (coklat, jahitan)
 *   pita gelap  uv(32, 28)   7 x 2 x 7
 *   tingkat 3   uv(0, 38)    6 x 2 x 6   (strip terang)
 *   tingkat 4   uv(24, 38)   5 x 2 x 5
 *   tingkat 5   uv(44, 38)   4 x 2 x 4
 *   ujung 1     uv(0, 46)    2 x 3 x 2   (child, miring ke belakang)
 *   ujung 2     uv(8, 46)    1 x 2 x 1   (child dari ujung 1, miring lagi)
 *   gesper      uv(16, 46)   3 x 2 x 1
 */
public class N3XRPurpleWitchHatModel {

        private static ModelPart model;

        /** Sudut tekuk ujung ke belakang (radian). Nilai negatif = ke belakang. */
        private static final float TIP_PITCH = -0.40f;
        private static final float TIP2_PITCH = -0.55f;

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
                                        .uv(16, 46).cuboid(-1.5f, -12.0f, -5.0f, 3.0f, 2.0f, 1.0f)
                                        .uv(32, 28).cuboid(-3.5f, -14.0f, -3.5f, 7.0f, 2.0f, 7.0f)
                                        .uv(0, 38).cuboid(-3.0f, -16.0f, -3.0f, 6.0f, 2.0f, 6.0f)
                                        .uv(24, 38).cuboid(-2.5f, -18.0f, -2.5f, 5.0f, 2.0f, 5.0f)
                                        .uv(44, 38).cuboid(-2.0f, -20.0f, -2.0f, 4.0f, 2.0f, 4.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        // ujung topi: pitch negatif = menekuk ke belakang (+z).
                        // Kalau di game malah ke depan, balik tandanya (jadi positif).
                        ModelPartData tip = hat.addChild(
                                "tip",
                                ModelPartBuilder.create()
                                        .uv(0, 46).cuboid(-1.0f, -3.0f, -1.0f, 2.0f, 3.0f, 2.0f),
                                ModelTransform.of(0.0f, -20.0f, 0.0f, TIP_PITCH, 0.0f, 0.0f)
                        );

                        tip.addChild(
                                "tip2",
                                ModelPartBuilder.create()
                                        .uv(8, 46).cuboid(-0.5f, -2.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                                ModelTransform.of(0.0f, -3.0f, 0.0f, TIP2_PITCH, 0.0f, 0.0f)
                        );

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 64);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
