package com.n3xr.hats;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Builder geometri Witch Hat N3XR.
 * Diterjemahin dari hasil export "Export Java Entity" Blockbench
 * (format Mojang mappings) ke Yarn/Fabric.
 *
 * Beda dari straw hat kemarin -- export ini udah bersih dari
 * awal: semua koordinat X/Z sudah center di 0 (nggak perlu
 * dihitung ulang), dan pivot bawaan (group3, group4, group)
 * di atasnya cuma offset (0,16,0)/(0,0,0)/(0,0,0) yang nggak
 * relevan buat kita -- makanya diabaikan begitu aja, geometri
 * utamanya ("group2" beserta 3 anak rotasinya) sudah pas
 * dipasang langsung di pivot (0,0,0) kepala.
 *
 * 2 box dari hasil export yang punya depth/width 0 (baris
 * texOffs(0,0).addBox(...,14,0.5,0) yang muncul 2x) sengaja
 * di-skip di sini karena ukurannya degenerate/nggak kelihatan
 * (nol di salah satu sisi = nggak ada volume sama sekali).
 */
public class N3XRWitchHatModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        // Brim (piringan lebar) + 2 lapis dasar kerucut topi.
                        ModelPartData cone = root.addChild(
                                "cone",
                                ModelPartBuilder.create()
                                        .uv(0, 0).cuboid(-7.0f, 0.0f, -7.0f, 14.0f, 0.5f, 14.0f)
                                        .uv(3, 17).cuboid(-4.0f, -0.9f, -4.0f, 8.0f, 0.9f, 8.0f, new Dilation(0.25f))
                                        .uv(0, 26).cuboid(-4.0f, -3.0f, -4.0f, 8.0f, 3.0f, 8.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        // 3 potongan kerucut yang dimiringkan (bikin efek "melengkung
                        // ke satu sisi" khas topi penyihir) -- child dari "cone",
                        // rotasi di sumbu roll (Z).
                        cone.addChild(
                                "tip_lower",
                                ModelPartBuilder.create().uv(0, 36)
                                        .cuboid(-4.5f, -2.0f, 1.0f, 2.5f, 4.0f, 2.0f),
                                ModelTransform.of(2.8f, -4.0f, -2.0f, 0.0f, 0.0f, 0.7854f)
                        );

                        cone.addChild(
                                "tip_mid",
                                ModelPartBuilder.create().uv(33, 35)
                                        .cuboid(-4.0f, -1.8f, 0.0f, 4.0f, 2.8f, 4.0f),
                                ModelTransform.of(1.5f, -3.0f, -2.0f, 0.0f, 0.0f, 0.3927f)
                        );

                        cone.addChild(
                                "tip_base",
                                ModelPartBuilder.create().uv(32, 26)
                                        .cuboid(-7.0f, -1.5f, -1.0f, 6.0f, 2.5f, 6.0f),
                                ModelTransform.of(3.5f, -1.0f, -2.0f, 0.0f, 0.0f, 0.3927f)
                        );

                        // 64x64 -- sesuai LayerDefinition.create(...,64,64) di file export.
                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 64);

                        // Return root -- otomatis render "cone" + 3 anaknya sekaligus.
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
