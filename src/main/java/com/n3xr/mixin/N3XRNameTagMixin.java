package com.n3xr.mixin;

import com.n3xr.nametag.N3XRNameTag;
import com.n3xr.online.N3XRUsers;
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
 * Sisipkan icon N3XR di label nama: buat player sendiri (third person) dan buat
 * player lain yang kelihatan pakai N3XR (lihat online/N3XRUsers).
 * Dirender ulang lewat renderLabelIfPresent bawaan (dengan penjaga supaya nggak rekursif).
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

                Text replaced;
                if (player == mc.player) {
                        // diri sendiri (third person, modul Name Tag nyala)
                        if (!N3XRNameTag.active(mc)) return;
                        replaced = N3XRNameTag.build(player);
                } else {
                        // player lain: icon cuma kalau dia kelihatan pakai N3XR (bit di data skin-nya nyala)
                        if (!N3XRUsers.hasFlag(player)) return;
                        replaced = N3XRNameTag.withIcon(text);
                }

                n3xr$busy = true;
                try {
                        this.renderLabelIfPresent(player, replaced, matrices, vertexConsumers, light, tickDelta);
                } finally {
                        n3xr$busy = false;
                }
                ci.cancel();
        }
}
