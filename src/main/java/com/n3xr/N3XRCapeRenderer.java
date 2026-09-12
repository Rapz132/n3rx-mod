package com.n3xr.cosmetic;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Builder geometri cape N3XR. Rendering yang sesungguhnya sekarang
 * dilakukan lewat N3XRCapeFeatureMixin, yang inject langsung ke
 * CapeFeatureRenderer.render() milik vanilla — bukan lagi lewat
 * WorldRenderEvents seperti versi sebelumnya. Class ini cuma
 * bertanggung jawab membangun dan menyediakan ModelPart cape-nya.
 */
public class N3XRCapeRenderer {

        private static ModelPart capeModel;

        public static ModelPart getOrBuildModel() {
                if (capeModel == null) {
                        ModelData modelData = new ModelData();
                        ModelPartData root = modelData.getRoot();
                        root.addChild(
                                "cape",
                                ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -1.0f, 10.0f, 16.0f, 1.0f),
                                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
                        );
                        // 64x32 = ukuran canvas standar Minecraft untuk cape,
                        // supaya template cape gratis dari internet (mc-capes.com,
                        // resourcepackcreator.com, skinmc.net/capes/editor, dll)
                        // bisa langsung dipakai tanpa perlu dikonversi ulang.
                        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 64, 32);
                        capeModel = texturedModelData.createModel().getChild("cape");
                }
                return capeModel;
        }
}
