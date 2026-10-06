package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import com.n3xr.cosmetic.N3XRCapeManager;
import com.n3xr.cosmetic.N3XRCapeRenderer;
import com.n3xr.cosmetic.N3XRExternalCapeManager;
import com.n3xr.hats.N3XRHatManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mengganti render cape vanilla dengan cape custom N3XR (inject ke
 * CapeFeatureRenderer.render() vanilla), SEKALIGUS render Hat
 * cosmetic (nempel ke head bone).
 *
 * CAPE berlaku untuk SEMUA player yang dilihat (bukan cuma diri sendiri):
 *   1) Kalau ini diri sendiri DAN ada cape N3XR lokal yang dipilih
 *      -> pakai itu (prioritas tertinggi).
 *   2) Kalau nggak, cek N3XRExternalCapeManager (OptiFine Cape API,
 *      berbasis username).
 *   3) Kalau dua-duanya nggak ada, BIARIN vanilla render seperti
 *      biasa (jangan di-cancel), supaya cape asli player lain tetap
 *      kelihatan normal.
 *
 * Fisika & posisi cape (termasuk saat sneak) sekarang PERSIS sama
 * dengan CapeFeatureRenderer vanilla: cape tertarik waktu lari,
 * naik waktu lompat, goyang waktu belok, dan nempel ke punggung
 * waktu sneak.
 *
 * HAT masih cuma buat diri sendiri (belum ada sumber eksternal).
 *
 * Hat & cape digabung di mixin YANG SAMA supaya nggak ada masalah
 * urutan eksekusi antar-mixin yang sama-sama nge-cancel() di HEAD.
 *
 * Class ini extends FeatureRenderer<T,M> (generic sama persis kayak
 * CapeFeatureRenderer aslinya) supaya bisa manggil getContextModel()
 * -- dipakai buat ambil transform head bone vanilla, jadi hat
 * otomatis noleh/nunduk bareng kepala.
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

                // Pakai elytra -> cape disembunyikan (sama kayak vanilla).
                if (capeTexture != null && !player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
                        boolean sneaking = player.isInSneakingPose();
                        boolean wearingChest = !player.getEquippedStack(EquipmentSlot.CHEST).isEmpty();

                        matrices.push();
                        matrices.translate(0.0F, 0.0F, 0.125F);

                        // --- fisika cape persis vanilla (CapeFeatureRenderer) ---
                        double dx = MathHelper.lerp((double) tickDelta, player.prevCapeX, player.capeX)
                                        - MathHelper.lerp((double) tickDelta, player.prevX, player.getX());
                        double dy = MathHelper.lerp((double) tickDelta, player.prevCapeY, player.capeY)
                                        - MathHelper.lerp((double) tickDelta, player.prevY, player.getY());
                        double dz = MathHelper.lerp((double) tickDelta, player.prevCapeZ, player.capeZ)
                                        - MathHelper.lerp((double) tickDelta, player.prevZ, player.getZ());

                        float bodyYawDeg = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);
                        double sinYaw = MathHelper.sin(bodyYawDeg * 0.017453292F);
                        double cosYaw = -MathHelper.cos(bodyYawDeg * 0.017453292F);

                        float lift = MathHelper.clamp((float) dy * 10.0F, -6.0F, 32.0F);
                        float back = MathHelper.clamp((float) (dx * sinYaw + dz * cosYaw) * 100.0F, 0.0F, 150.0F);
                        float side = MathHelper.clamp((float) (dx * cosYaw - dz * sinYaw) * 100.0F, -20.0F, 20.0F);

                        float stride = MathHelper.lerp(tickDelta, player.prevStrideDistance, player.strideDistance);
                        lift += MathHelper.sin(MathHelper.lerp(tickDelta, player.prevHorizontalSpeed, player.horizontalSpeed) * 6.0F)
                                        * 32.0F * stride;

                        if (sneaking) lift += 25.0F;

                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + back / 2.0F + lift));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side / 2.0F));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - side / 2.0F));

                        // --- posisi cape saat sneak, nilai sama kayak vanilla ---
                        ModelPart capeModel = N3XRCapeRenderer.getOrBuildModel();
                        capeModel.pitch = 0.0F;
                        capeModel.yaw = 0.0F;
                        capeModel.roll = 0.0F;
                        capeModel.pivotX = 0.0F;
                        capeModel.pivotY = sneaking ? (wearingChest ? 0.8F : 1.85F) : 0.0F;
                        capeModel.pivotZ = sneaking ? (wearingChest ? 0.3F : 1.4F) : 0.0F;

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
