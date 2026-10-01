package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import com.n3xr.cosmetic.N3XRCapeManager;
import com.n3xr.cosmetic.N3XRCapeRenderer;
import com.n3xr.cosmetic.N3XRExternalCapeManager;
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
 * CAPE sekarang berlaku untuk SEMUA player yang dilihat (bukan cuma
 * diri sendiri):
 *   1) Kalau ini diri sendiri DAN ada cape N3XR lokal yang dipilih
 *      -> pakai itu (prioritas tertinggi).
 *   2) Kalau nggak, cek N3XRExternalCapeManager (OptiFine Cape API,
 *      berbasis username) -- berlaku untuk SIAPA SAJA, nggak cuma
 *      diri sendiri, supaya sesama pengguna N3XR saling lihat cape
 *      masing-masing kalau mereka terdaftar di layanan itu.
 *   3) Kalau dua-duanya nggak ada, BIARIN vanilla render seperti
 *      biasa (jangan di-cancel) -- supaya cape asli Mojang/premium
 *      player lain (yang bukan user N3XR) tetap kelihatan normal,
 *      nggak ke-hide gara-gara mixin ini.
 *
 * HAT masih cuma buat diri sendiri (belum ada sumber eksternal
 * buat hat).
 *
 * Hat & cape digabung di mixin YANG SAMA (bukan terpisah) supaya
 * nggak ada masalah urutan eksekusi antar-mixin yang sama-sama
 * nge-cancel() di titik HEAD yang sama.
 *
 * Class ini extends FeatureRenderer<T,M> (generic sama persis kayak
 * CapeFeatureRenderer aslinya) supaya bisa manggil
 * this.getContextModel() -- dipakai buat ambil transform head bone
 * ASLI vanilla, jadi hat otomatis noleh/nunduk bareng kepala.
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
                if (mc.player == null) return;

                if (player.isInvisible()) {
                        ci.cancel();
                        return;
                }

                boolean isSelf = player == mc.player;
                boolean renderedSomething = false;

                // ================= CAPE =================
                Identifier capeTexture = null;
                if (isSelf && N3XRConfig.capeSelectedKey != null) {
                        capeTexture = N3XRCapeManager.getSelectedTexture();
                }
                if (capeTexture == null) {
                        capeTexture = N3XRExternalCapeManager.getCapeTexture(player.getGameProfile().getName());
                }

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
                        renderedSomething = true;
                }

                // ================= HAT (cuma diri sendiri) =================
                if (isSelf && N3XRConfig.hatSelectedKey != null) {
                        Identifier hatTexture = N3XRHatManager.getSelectedTexture();
                        ModelPart hatModel = N3XRHatManager.getSelectedModel();
                        if (hatTexture != null && hatModel != null) {
                                matrices.push();

                                this.getContextModel().head.rotate(matrices);

                                VertexConsumer hatConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(hatTexture));
                                hatModel.render(matrices, hatConsumer, light, OverlayTexture.DEFAULT_UV);

                                matrices.pop();
                                renderedSomething = true;
                        }
                }

                if (renderedSomething) {
                        ci.cancel();
                }
        }
}
