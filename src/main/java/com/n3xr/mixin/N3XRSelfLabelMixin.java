package com.n3xr.mixin;

import com.n3xr.nametag.N3XRNameTag;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla nggak nampilin nametag buat player yang lagi jadi kamera (diri sendiri).
 * Mixin ini maksa label tetap dirender pas third person, supaya
 * N3XRNameTagMixin bisa nyisipin icon N3XR.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class N3XRSelfLabelMixin {

        @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("RETURN"), cancellable = true, require = 0)
        private void n3xr$forceSelfLabel(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
                if (cir.getReturnValue()) return;

                MinecraftClient mc = MinecraftClient.getInstance();
                if (entity != mc.player) return;
                if (!MinecraftClient.isHudEnabled()) return;
                if (entity.isInvisible()) return;
                if (!N3XRNameTag.active(mc)) return;

                N3XRNameTag.handledByMixin = true;
                cir.setReturnValue(true);
        }
}
