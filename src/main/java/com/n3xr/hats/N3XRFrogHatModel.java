package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Frog Hat: topi bucket hijau dengan dua mata katak besar menonjol di atas
 * (pupil hitam menghadap depan, mengarah ke dalam). Dibuat dari kode.
 *
 * Koordinat relatif ke pivot kepala: y = -8 puncak kepala, depan = -z.
 * Texture: textures/hats/frog_hat.png, 64x32. Layout box UV:
 *   band bawah   uv(0, 0)    10 x 3 x 10   (menutupi dahi, sedikit lebih lebar dari kepala)
 *   mahkota      uv(0, 13)    9 x 2 x 9
 *   mata kiri    uv(0, 24)    4 x 3 x 3    (sisi -x, pupil di sisi dalam)
 *   mata kanan   uv(14, 24)   4 x 3 x 3    (sisi +x, pupil di sisi dalam)
 */
public class N3XRFrogHatModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        root.addChild(
                                "frog_hat",
                                ModelPartBuilder.create()
                                        .uv(0, 0).cuboid(-5.0f, -9.0f, -5.0f, 10.0f, 3.0f, 10.0f)
                                        .uv(0, 13).cuboid(-4.5f, -11.0f, -4.5f, 9.0f, 2.0f, 9.0f)
                                        .uv(0, 24).cuboid(-4.5f, -14.0f, -4.5f, 4.0f, 3.0f, 3.0f)
                                        .uv(14, 24).cuboid(0.5f, -14.0f, -4.5f, 4.0f, 3.0f, 3.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 32);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
