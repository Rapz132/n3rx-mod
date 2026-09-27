package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Builder geometri Horn (tanduk) N3XR.
 * Diterjemahin dari hasil export "Export Java Entity" Blockbench
 * (format Mojang mappings) ke Yarn/Fabric.
 *
 * Sama kayak witch hat -- semua Y digeser -8 dari hasil export
 * mentah, karena originnya keliatan nganggep "0 = pas di kepala",
 * padahal pivot yang kita pakai (head.rotate()) itu di DASAR kepala
 * (leher). X/Z nggak perlu di-center ulang, udah simetris dari
 * sononya di file aslinya.
 *
 * Texture 64x64 (sesuai info dari user -- file export nulis 32x32,
 * tapi file texture asli yang dipakai 64x64).
 */
public class N3XRHornModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        // Dasar tanduk kanan & kiri.
                        ModelPartData base = root.addChild(
                                "base",
                                ModelPartBuilder.create()
                                        .uv(0, 0).cuboid(3.0f, -8.0f, -1.0f, 3.0f, 2.0f, 2.0f)
                                        .uv(0, 4).cuboid(-6.0f, -8.0f, -1.0f, 3.0f, 2.0f, 2.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        // 4 segmen melengkung yang bikin bentuk tanduk melintir.
                        base.addChild(
                                "horn_r1",
                                ModelPartBuilder.create().uv(0, 8)
                                        .cuboid(-2.0f, -1.0f, 0.0f, 2.0f, 3.0f, 2.0f),
                                ModelTransform.of(-7.0389f, -10.7344f, -1.0f, 0.0f, 0.0f, -0.3927f)
                        );

                        base.addChild(
                                "horn_r2",
                                ModelPartBuilder.create().uv(10, 0)
                                        .cuboid(-2.0f, -1.0f, 0.0f, 2.0f, 3.0f, 2.0f),
                                ModelTransform.of(8.8867f, -9.9691f, -1.0f, 0.0f, 0.0f, 0.3927f)
                        );

                        base.addChild(
                                "horn_r3",
                                ModelPartBuilder.create().uv(0, 13)
                                        .cuboid(0.0f, -2.0f, 0.0f, 2.0f, 3.0f, 2.0f),
                                ModelTransform.of(-6.7071f, -6.7071f, -1.0f, 0.0f, 0.0f, -0.7854f)
                        );

                        base.addChild(
                                "horn_r4",
                                ModelPartBuilder.create().uv(8, 8)
                                        .cuboid(-2.0f, -1.0f, 0.0f, 2.0f, 3.0f, 2.0f),
                                ModelTransform.of(7.4142f, -7.4142f, -1.0f, 0.0f, 0.0f, 0.7854f)
                        );

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 64);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
