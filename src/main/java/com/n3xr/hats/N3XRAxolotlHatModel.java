package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Axolotl Hat: axolotl pink yang rebahan di atas kepala -- pelat bawah (kaki),
 * badan, kepala dengan mata ungu tua dan mulut, insang gelap di kiri-kanan
 * kepala (dua miring ke atas + satu datar per sisi), dan ekor di belakang.
 * Dibuat dari kode (tanpa Blockbench).
 *
 * Koordinat relatif ke pivot kepala: y = -8 puncak kepala, depan = -z.
 * Texture: textures/hats/axolotl_hat.png, 64x64. Layout box UV:
 *   pelat bawah  uv(0, 0)    12 x 1 x 12
 *   badan        uv(0, 13)   10 x 2 x 11
 *   kepala       uv(0, 26)    8 x 4 x 8    (wajah di sisi depan)
 *   insang datar uv(32, 26)   2 x 1 x 3
 *   insang miring uv(42, 26)  3 x 1 x 3
 *   ekor         uv(0, 38)    3 x 1 x 4
 *   sirip ekor   uv(14, 38)   1 x 2 x 2
 */
public class N3XRAxolotlHatModel {

        private static ModelPart model;

        /** Kemiringan insang atas (radian). Naikkan untuk lebih tegak. */
        private static final float GILL_TILT = 0.75f;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        ModelPartData hat = root.addChild(
                                "axolotl_hat",
                                ModelPartBuilder.create()
                                        // pelat bawah, badan, kepala
                                        .uv(0, 0).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f)
                                        .uv(0, 13).cuboid(-5.0f, -11.0f, -5.0f, 10.0f, 2.0f, 11.0f)
                                        .uv(0, 26).cuboid(-4.0f, -15.0f, -5.0f, 8.0f, 4.0f, 8.0f)
                                        // insang datar (kanan dan kiri)
                                        .uv(32, 26).cuboid(4.0f, -12.0f, -2.0f, 2.0f, 1.0f, 3.0f)
                                        .uv(32, 26).cuboid(-6.0f, -12.0f, -2.0f, 2.0f, 1.0f, 3.0f)
                                        // ekor + sirip
                                        .uv(0, 38).cuboid(-1.5f, -10.5f, 6.0f, 3.0f, 1.0f, 4.0f)
                                        .uv(14, 38).cuboid(-0.5f, -11.5f, 9.0f, 1.0f, 2.0f, 2.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        // dua insang miring per sisi (menjulur ke luar dan ke atas)
                        hat.addChild("gill_r1",
                                ModelPartBuilder.create().uv(42, 26).cuboid(0.0f, -0.5f, 0.0f, 3.0f, 1.0f, 3.0f),
                                ModelTransform.of(3.5f, -14.0f, -3.5f, 0.0f, 0.0f, -GILL_TILT));
                        hat.addChild("gill_r2",
                                ModelPartBuilder.create().uv(42, 26).cuboid(0.0f, -0.5f, 0.0f, 3.0f, 1.0f, 3.0f),
                                ModelTransform.of(3.5f, -13.0f, -0.5f, 0.0f, 0.0f, -GILL_TILT * 0.6f));
                        hat.addChild("gill_l1",
                                ModelPartBuilder.create().uv(42, 26).cuboid(-3.0f, -0.5f, 0.0f, 3.0f, 1.0f, 3.0f),
                                ModelTransform.of(-3.5f, -14.0f, -3.5f, 0.0f, 0.0f, GILL_TILT));
                        hat.addChild("gill_l2",
                                ModelPartBuilder.create().uv(42, 26).cuboid(-3.0f, -0.5f, 0.0f, 3.0f, 1.0f, 3.0f),
                                ModelTransform.of(-3.5f, -13.0f, -0.5f, 0.0f, 0.0f, GILL_TILT * 0.6f));

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 64);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
