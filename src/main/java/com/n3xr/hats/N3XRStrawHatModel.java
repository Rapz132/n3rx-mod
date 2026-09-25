package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Builder geometri Straw Hat (topi jerami) N3XR.
 * Diterjemahin dari hasil export "Export Java Entity" Blockbench
 * (format Mojang mappings) ke Yarn/Fabric.
 *
 * PENTING -- perubahan dari hasil export mentah:
 * 1) Hasil export mentahnya pakai "bone" pivot (8, 24, -8) yang cuma
 *    default bawaan Blockbench buat entity, BUKAN posisi yang kita
 *    mau (kepala player). Nilai itu udah dilebur ke koordinat cuboid
 *    di bawah, dan pivot di-set ulang ke (0,0,0) -- konsisten sama
 *    N3XRCapeRenderer/N3XRStrawHatModel versi sebelumnya.
 * 2) Dua cuboid hasil export ternyata TIDAK simetris satu sama lain
 *    (kemungkinan sisa dari proses geser-geser manual sebelumnya).
 *    Di bawah ini masing-masing cuboid di-center ULANG secara
 *    independen (X dan Z dipusatkan ke 0 sendiri-sendiri), supaya
 *    hasilnya beneran simetris nangkring di kepala, bukan nyodok ke
 *    satu sisi.
 * 3) Ukuran texture 128x128 (sesuai file texture yang dipakai) --
 *    UV offset (texOffs) dari hasil export dipakai apa adanya karena
 *    memang dihitung berdasarkan kanvas 128x128 itu.
 */
public class N3XRStrawHatModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        // Brim: pinggiran lebar & tipis, dari addBox(-28,-8,8, 20,0,19)
                        // di pivot bone (8,24,-8) -> dileburin jadi x[-20,0] y[16,16] z[0,19],
                        // lalu di-center ulang jadi x[-10,10] z[-9.5,9.5], tebal dinaikkan
                        // ke 1px (dari 0px asli, supaya nggak degenerate).
                        root.addChild(
                                "brim",
                                ModelPartBuilder.create().uv(20, 0)
                                        .cuboid(-10.0f, 8.0f, -9.5f, 20.0f, 1.0f, 19.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        // Crown: bagian yang menutup kepala, dari addBox(-20,-13,8, 12,5,9)
                        // -> dileburin jadi x[-12,0] y[11,16] z[0,9], di-center ulang jadi
                        // x[-6,6] z[-4.5,4.5], dan Y digeser supaya dasarnya nempel di y=8
                        // (puncak kepala vanilla) -> y[8,13].
                        root.addChild(
                                "crown",
                                ModelPartBuilder.create().uv(12, 9)
                                        .cuboid(-6.0f, 8.0f, -4.5f, 12.0f, 5.0f, 9.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        // 128x128 -- ukuran texture yang dipakai untuk straw hat.
                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 128, 128);

                        // Return root supaya crown + brim ke-render bareng sekaligus
                        // (ModelPart.render() otomatis render semua child-nya juga).
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
