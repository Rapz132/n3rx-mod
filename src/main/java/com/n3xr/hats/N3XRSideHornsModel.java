package com.n3xr.hats;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Horns: sepasang tanduk di sisi kanan-kiri kepala, melengkung keluar lalu
 * ke atas. Dibuat dari kode (4 kotak per tanduk, kiri-kanan simetris),
 * tanpa file model Blockbench.
 *
 * Koordinat relatif ke pivot kepala: kepala x -4..4, y -8..0, z -4..4.
 * Tanduk mulai dari sisi kepala (x = ±4) setinggi pelipis.
 *
 * Texture: textures/hats/horns.png, 16x16. Kiri dan kanan memakai area UV
 * yang sama (desainnya simetris), warnanya gradasi dari pangkal (gelap)
 * ke ujung (terang).
 *   A pangkal  uv(0, 0)   2 x 2 x 2
 *   B          uv(0, 4)   2 x 2 x 2
 *   C          uv(0, 8)   2 x 2 x 2
 *   D ujung    uv(0, 12)  1 x 2 x 1
 */
public class N3XRSideHornsModel {

        private static ModelPart model;

        // {x1, y1, z1, sizeX, sizeY, sizeZ, u, v} untuk tanduk sisi kanan (x positif)
        private static final float[][] SEGMENTS = {
                {4f, -7f, -1f, 2f, 2f, 2f, 0, 0},
                {6f, -8f, -1f, 2f, 2f, 2f, 0, 4},
                {7f, -10f, -1f, 2f, 2f, 2f, 0, 8},
                {8f, -12f, -0.5f, 1f, 2f, 1f, 0, 12},
        };

        public static ModelPart getOrBuildModel() {
                if (model == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();

                        ModelPartBuilder builder = ModelPartBuilder.create();
                        for (float[] s : SEGMENTS) {
                                int u = (int) s[6];
                                int v = (int) s[7];
                                // kanan
                                builder.uv(u, v).cuboid(s[0], s[1], s[2], s[3], s[4], s[5]);
                                // kiri (cermin sumbu X)
                                builder.uv(u, v).cuboid(-(s[0] + s[3]), s[1], s[2], s[3], s[4], s[5]);
                        }

                        root.addChild("horns", builder, ModelTransform.pivot(0.0f, 0.0f, 0.0f));

                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 16, 16);
                        model = texturedModelData.createModel();
                }
                return model;
        }
}
