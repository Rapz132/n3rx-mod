package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Builder geometri China Hat (topi kerucut bertingkat ala caping/
 * topi Tiongkok) N3XR. Diterjemahin dari hasil export "Export Java
 * Entity" Blockbench (format Mojang mappings) ke Yarn/Fabric.
 *
 * X/Z di-center ulang ke 0 (di file aslinya nggak simetris -- semua
 * box condong ke X negatif & Z positif). Y TIDAK digeser -- box dasar
 * (yang nutup kepala) udah pas span dari y=-8 (puncak kepala) sampai
 * y=0 (leher), pas sama konvensi pivot head.rotate() yang kita pakai
 * (beda dari witch hat/horn yang originnya masih nganggep "0 = di
 * kepala").
 *
 * 1 box dari hasil export (texOffs(0,0), tinggi/sizeY = 0) di-skip
 * karena degenerate/nggak ada volume, sama kayak kasus straw hat.
 *
 * Texture 128x128 (sesuai file asli & juga sesuai
 * LayerDefinition.create(...,128,128) di file export).
 */
public class N3XRChinaHatModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        root.addChild(
                                "china_hat",
                                ModelPartBuilder.create()
                                        .uv(17, 34).cuboid(-4.5f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f)
                                        .uv(0, 29).cuboid(-4.5f, -11.05f, -5.0f, 9.0f, 1.05f, 10.0f)
                                        .uv(0, 16).cuboid(-5.5f, -10.05f, -6.0f, 11.0f, 1.05f, 12.0f)
                                        .uv(38, 29).cuboid(-3.5f, -12.05f, -4.0f, 7.0f, 1.05f, 8.0f)
                                        .uv(38, 38).cuboid(-2.5f, -13.05f, -3.0f, 5.0f, 1.05f, 6.0f)
                                        .uv(0, 40).cuboid(-1.5f, -14.05f, -2.0f, 3.0f, 1.05f, 4.0f)
                                        .uv(14, 40).cuboid(-0.5f, -15.05f, -1.0f, 1.0f, 1.05f, 2.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 128, 128);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
