package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Builder geometri Straw Hat (topi jerami) N3XR.
 *
 * Koordinat di bawah ini BUKAN hasil geser-geser manual di Blockbench,
 * tapi dihitung langsung dari data mentah glTF (posisi vertex asli +
 * transform node/rotasi, dikonversi dari satuan block ke pixel
 * Minecraft, lalu di-center: X/Z dipusatkan ke 0, dan bagian bawah
 * topi disejajarkan ke y=8 -- persis puncak kepala vanilla (kepala
 * player tingginya 8px, dari y=0 sampai y=8).
 *
 * Dua cuboid:
 *   1) "crown"  -- bagian atas topi yang nutup kepala (8.5 x 3 x 8.5 px)
 *   2) "brim"   -- pinggiran lebar & tipis (14 x 1 x 14 px), nempel
 *                  rata di dasar crown (y=8)
 *
 * UV di bawah masih placeholder (0,0) -- silakan sesuaikan lewat
 * trial-and-error di game (mirip proses nyari offset cape kemarin),
 * atau screenshot in-game-nya nanti biar saya bantu pas-in ke texture
 * gltf_embedded_0.png (atlas straw hat, 64x64px).
 */
public class N3XRStrawHatModel {

        private static ModelPart model;

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        // Crown: bagian yang menutup atas kepala.
                        root.addChild(
                                "crown",
                                ModelPartBuilder.create().uv(0, 0)
                                        .cuboid(-4.25f, 8.0f, -4.25f, 8.5f, 3.0f, 8.5f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        // Brim: pinggiran lebar & tipis di dasar crown.
                        root.addChild(
                                "brim",
                                ModelPartBuilder.create().uv(0, 12)
                                        .cuboid(-7.0f, 8.0f, -7.0f, 14.0f, 1.0f, 14.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );

                        // 64x64 -- ukuran atlas gltf_embedded_0.png (straw hat berbagi
                        // atlas yang sama dengan reference skin/paper bag).
                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 64);
                        ModelPart root2 = texturedModelData.createModel();

                        // Gabungkan crown + brim jadi satu ModelPart induk supaya bisa
                        // di-render & di-rotate sekaligus (ngikut kepala) lewat satu
                        // pivot yang sama, persis pola N3XRCapeRenderer.
                        model = root2;
                }
                return model;
        }
}
