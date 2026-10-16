package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Fungi Hat: topi jamur merah bercak putih -- tudung membulat bertingkat
 * (kubah) dengan insang krem di bawahnya, plus satu jamur kecil tumbuh di
 * atasnya. Dibuat dari kode (tanpa Blockbench).
 *
 * Koordinat relatif ke pivot kepala: y = -8 puncak kepala, hat duduk di atasnya.
 * Texture: textures/hats/fungi_hat.png, 64x64. Layout box UV:
 *   insang       uv(0, 0)    12 x 1 x 12   (bawah: pola insang radial krem)
 *   tudung 1     uv(0, 13)   12 x 2 x 12
 *   tudung 2     uv(0, 27)   10 x 2 x 10
 *   tudung 3     uv(0, 39)    8 x 1 x 8
 *   tudung 4     uv(32, 39)   5 x 1 x 5
 *   batang kecil uv(0, 48)    1 x 2 x 1
 *   tudung kecil uv(4, 48)    3 x 1 x 3
 */
public class N3XRFungiHatModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        root.addChild(
                                "fungi_hat",
                                ModelPartBuilder.create()
                                        .uv(0, 0).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f)
                                        .uv(0, 13).cuboid(-6.0f, -11.0f, -6.0f, 12.0f, 2.0f, 12.0f)
                                        .uv(0, 27).cuboid(-5.0f, -13.0f, -5.0f, 10.0f, 2.0f, 10.0f)
                                        .uv(0, 39).cuboid(-4.0f, -14.0f, -4.0f, 8.0f, 1.0f, 8.0f)
                                        .uv(32, 39).cuboid(-2.5f, -15.0f, -2.5f, 5.0f, 1.0f, 5.0f)
                                        // jamur kecil di belakang-kiri atas
                                        .uv(0, 48).cuboid(2.5f, -16.0f, 1.5f, 1.0f, 2.0f, 1.0f)
                                        .uv(4, 48).cuboid(1.5f, -17.0f, 0.5f, 3.0f, 1.0f, 3.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 64);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
