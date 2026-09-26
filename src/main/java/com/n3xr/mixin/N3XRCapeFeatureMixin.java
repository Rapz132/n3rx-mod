package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import com.n3xr.cosmetic.N3XRCapeManager;
import com.n3xr.cosmetic.N3XRCapeRenderer;
import com.n3xr.hats.N3XRHatManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mengganti render cape vanilla dengan cape custom N3XR (inject ke
 * CapeFeatureRenderer.render() vanilla), SEKALIGUS render Hat
 * cosmetic (nempel ke head bone).
 *
 * Hat digabung di mixin YANG SAMA dengan cape (bukan mixin
 * terpisah) supaya nggak ada masalah urutan eksekusi: cape mixin
 * ini nge-cancel() render vanilla di akhir, dan kalau ada 2 mixin
 * beda yang sama-sama @Inject di titik HEAD method yang sama, Mixin
 * framework nggak menjamin urutan render-nya -- bisa jadi salah
 * satu (cape atau hat) nggak ke-render sama sekali tergantung mana
 * yang jalan duluan. Digabung di sini, urutannya pasti: cape dulu,
 * baru hat, baru cancel.
 *
 * Class ini sekarang extends FeatureRenderer<T,M> (generic sama
 * persis kayak CapeFeatureRenderer aslinya) supaya bisa manggil
 * this.getContextModel() -- dipakai buat ambil transform head bone
 * ASLI vanilla (posisi + rotasi ngikut arah pandang kepala player
 * saat itu), jadi hat otomatis noleh/nunduk bareng kepala tanpa kita
 * itung ulang rotasi manual sendiri (beda dari cape yang emang harus
 * dihitung manual karena capenya "ngayun", bukan solid nempel).
 */
@Mixin(CapeFeatureRenderer.class)
public abstract class N3XRCapeFeatureMixin<T extends AbstractClientPlayerEntity, M extends PlayerEntityModel<T>>
                extends FeatureRenderer<T, M> {

        public N3XRCapeFeatureMixin(FeatureRendererContext<T, M> context) {
                super(context);
        }

        @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
        private void n3xr$renderCapeAndHat(
                MatrixStack matrices,
                VertexConsumerProvider vertexConsumers,
                int light,
                T player,
                float limbAngle,
                float limbDistance,
                float tickDelta,
                float animationProgress,
                float headYaw,
                float headPitch,
                CallbackInfo ci
        ) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null || player != mc.player) return;

                if (player.isInvisible()) {
                        ci.cancel();
                        return;
                }

                // ================= CAPE =================
                if (N3XRConfig.capeSelectedKey != null) {
                        Identifier capeTexture = N3XRCapeManager.getSelectedTexture();
                        if (capeTexture != null) {
                                matrices.push();

                                boolean sneaking = player.isInSneakingPose();

                                double zOffset = sneaking ? 0.45 : 0.3;
                                double yOffset = sneaking ? -0.2 : 0.0;
                                matrices.translate(0.0, yOffset, zOffset);

                                ModelPart capeModel = N3XRCapeRenderer.getOrBuildModel();

                                // Vanilla CapeFeatureRenderer selalu memutar model cape
                                // 180 derajat di sumbu Y sebelum render.
                                capeModel.yaw = (float) Math.PI;

                                float baseTilt = sneaking ? 30.0f : 6.0f;
                                float swing = MathHelper.sin(player.age * 0.2f) * 6.0f;
                                capeModel.pitch = (float) Math.toRadians(baseTilt + swing);

                                VertexConsumer capeConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(capeTexture));
                                capeModel.render(matrices, capeConsumer, light, OverlayTexture.DEFAULT_UV);

                                matrices.pop();
                        }
                }

                // ================= HAT =================
                if (N3XRConfig.hatSelectedKey != null) {
                        Identifier hatTexture = N3XRHatManager.getSelectedTexture();
                        ModelPart hatModel = N3XRHatManager.getSelectedModel();
                        if (hatTexture != null && hatModel != null) {
                                matrices.push();

                                // Pindah ke transform head bone ASLI vanilla (pivot +
                                // rotasi yang udah dihitung vanilla buat frame ini),
                                // supaya hat otomatis ngikut arah pandang kepala.
                                // N3XRStrawHatModel geometrinya udah didesain pakai
                                // konvensi Y+ ke bawah (sama kayak vanilla), jadi nggak
                                // perlu flip/scale tambahan di sini lagi.
                                this.getContextModel().head.rotate(matrices);

                                VertexConsumer hatConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(hatTexture));
                                hatModel.render(matrices, hatConsumer, light, OverlayTexture.DEFAULT_UV);

                                matrices.pop();
                        }
                }

                ci.cancel();
        }
}
