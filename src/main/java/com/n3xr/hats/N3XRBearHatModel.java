package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Bear Hat: topi beruang cokelat berbulu -- penutup kepala dengan dua telinga
 * bulat di atas (bagian dalam krem), dua mata kecil, dan moncong krem berhidung
 * hitam di dahi, jadi wajah beruang muncul di atas wajah pemain. Dibuat dari kode.
 *
 * Koordinat relatif ke pivot kepala: y = -8 puncak kepala, depan = -z.
 * Texture: textures/hats/bear_hat.png, 64x32. Layout box UV:
 *   cap bawah   uv(0, 0)    10 x 3 x 10   (menutupi dahi)
 *   cap atas    uv(0, 13)    9 x 2 x 9    (mata di sisi depan)
 *   telinga     uv(0, 24)    3 x 2 x 2    (kiri dan kanan memakai UV yang sama)
 *   puncak telinga uv(10, 24) 1 x 1 x 2
 *   moncong     uv(16, 24)   4 x 2 x 1    (hidung + mulut di sisi depan)
 */
public class N3XRBearHatModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        root.addChild(
                                "bear_hat",
                                ModelPartBuilder.create()
                                        .uv(0, 0).cuboid(-5.0f, -10.0f, -5.0f, 10.0f, 3.0f, 10.0f)
                                        .uv(0, 13).cuboid(-4.5f, -12.0f, -4.5f, 9.0f, 2.0f, 9.0f)
                                        // telinga kiri dan kanan (dasar + puncak, biar terlihat membulat)
                                        .uv(0, 24).cuboid(-5.0f, -14.0f, -2.0f, 3.0f, 2.0f, 2.0f)
                                        .uv(10, 24).cuboid(-4.0f, -15.0f, -2.0f, 1.0f, 1.0f, 2.0f)
                                        .uv(0, 24).cuboid(2.0f, -14.0f, -2.0f, 3.0f, 2.0f, 2.0f)
                                        .uv(10, 24).cuboid(3.0f, -15.0f, -2.0f, 1.0f, 1.0f, 2.0f)
                                        // moncong di dahi
                                        .uv(16, 24).cuboid(-2.0f, -9.0f, -6.0f, 4.0f, 2.0f, 1.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 32);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
