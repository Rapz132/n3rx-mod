package com.n3xr.mixin;

import com.n3xr.nametag.N3XRNameTag;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ganti teks label player sendiri jadi "[ICON] nama  HP" lalu render ulang
 * lewat renderLabelIfPresent bawaan (dengan penjaga supaya nggak rekursif).
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class N3XRNameTagMixin {

        @Shadow
        protected abstract void renderLabelIfPresent(AbstractClientPlayerEntity player, Text text,
                        MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta);

        private boolean n3xr$busy = false;

        @Inject(
                method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V",
                at = @At("HEAD"), cancellable = true, require = 0)
        private void n3xr$addIcon(AbstractClientPlayerEntity player, Text text, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
                if (n3xr$busy) return;

                MinecraftClient mc = MinecraftClient.getInstance();
                if (player != mc.player || !N3XRNameTag.active(mc)) return;

                n3xr$busy = true;
                try {
                        this.renderLabelIfPresent(player, N3XRNameTag.build(player), matrices, vertexConsumers, light, tickDelta);
                } finally {
                        n3xr$busy = false;
                }
                ci.cancel();
        }
}
