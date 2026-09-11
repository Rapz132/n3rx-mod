package com.n3xr.cosmetic;

import com.n3xr.N3XRConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * Render cape custom di punggung player, dipanggil lewat
 * WorldRenderEvents.AFTER_ENTITIES (bukan mixin ke constructor
 * PlayerEntityRenderer) supaya lebih aman dan konsisten dengan
 * pendekatan nametag yang sudah terbukti stabil.
 *
 * Cape mengikuti rotasi badan player (bukan billboard menghadap
 * kamera seperti nametag), sehingga terlihat menempel wajar saat
 * player berputar.
 */
public class N3XRCapeRenderer {

        private static ModelPart capeModel;

        private static ModelPart getOrBuildModel() {
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

        public static void render(WorldRenderContext context) {
                if (N3XRConfig.capeSelectedKey == null) return;

                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null || mc.world == null) return;

                Identifier capeTexture = N3XRCapeManager.getSelectedTexture();
                if (capeTexture == null) return;

                PlayerEntity player = mc.player;

                var camPos = context.camera().getPos();

                double x = player.getX() - camPos.x;
                double y = player.getY() - camPos.y;
                double z = player.getZ() - camPos.z;

                MatrixStack matrices = context.matrixStack();
                matrices.push();

                matrices.translate(x, y, z);

                float bodyYaw = player.getBodyYaw();
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));

                matrices.translate(0.0, player.getHeight() * 0.72, 0.08);

                float swing = MathHelper.sin(player.age * 0.15f) * 2.0f;
                ModelPart model = getOrBuildModel();
                model.pitch = (float) Math.toRadians(6.0 + swing);

                var vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(capeTexture));
                model.render(matrices, vertexConsumer, 0xF000F0, OverlayTexture.DEFAULT_UV);
                vertexConsumers.draw();

                matrices.pop();
        }
}
