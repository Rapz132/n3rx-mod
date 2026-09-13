package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import com.n3xr.cosmetic.N3XRCapeManager;
import com.n3xr.cosmetic.N3XRCapeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mengganti render cape vanilla dengan cape custom N3XR, dengan
 * inject langsung ke CapeFeatureRenderer.render() milik vanilla
 * (bukan WorldRenderEvents seperti percobaan sebelumnya).
 *
 * Keuntungan pendekatan ini: matrices yang diterima di titik ini
 * SUDAH berada di ruang model-space yang benar (posisi, rotasi
 * badan, dan skala 1/16-blok sudah diterapkan otomatis oleh
 * LivingEntityRenderer sebelum method ini dipanggil) — sehingga
 * tidak perlu menghitung ulang translate/rotate/scale secara
 * manual seperti pada implementasi WorldRenderEvents sebelumnya,
 * yang berulang kali salah (posisi "ngawur").
 *
 * Hanya diterapkan untuk local player (fitur cosmetic ini
 * client-side, hanya terlihat oleh diri sendiri). Jika
 * capeSelectedKey null, method dibiarkan berjalan normal (cape
 * vanilla resmi, jika ada, tetap tampil apa adanya).
 *
 * require = 0 supaya jika signature CapeFeatureRenderer.render()
 * berbeda di versi Minecraft lain, hanya fitur ini yang tidak
 * aktif tanpa menjatuhkan seluruh mod.
 */
@Mixin(CapeFeatureRenderer.class)
public abstract class N3XRCapeFeatureMixin {

        @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
        private void n3xr$renderCustomCape(
                MatrixStack matrices,
                VertexConsumerProvider vertexConsumers,
                int light,
                AbstractClientPlayerEntity player,
                float limbAngle,
                float limbDistance,
                float tickDelta,
                float animationProgress,
                float headYaw,
                float headPitch,
                CallbackInfo ci
        ) {
                if (N3XRConfig.capeSelectedKey == null) return;

                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null || player != mc.player) return;

                Identifier capeTexture = N3XRCapeManager.getSelectedTexture();
                if (capeTexture == null) return;

                if (player.isInvisible()) {
                        ci.cancel();
                        return;
                }

                matrices.push();

                // Offset ini meniru posisi cape vanilla (menempel sedikit
                // di belakang punggung). Digedein signifikan dari standar
                // vanilla (0.125) supaya ada celah jelas dari badan, tidak
                // terlihat menyatu/menempel rata seperti sebelumnya.
                matrices.translate(0.0, 0.0, 0.3);

                ModelPart model = N3XRCapeRenderer.getOrBuildModel();

                // Tilt tetap ke luar + sedikit goyangan mengikuti waktu,
                // supaya cape terlihat seperti kain terpisah yang
                // menggantung, bukan menempel kaku ke badan.
                float swing = MathHelper.sin(player.age * 0.15f) * 2.0f;
                model.pitch = (float) Math.toRadians(6.0 + swing);

                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(capeTexture));
                model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);

                matrices.pop();

                ci.cancel();
        }
}
