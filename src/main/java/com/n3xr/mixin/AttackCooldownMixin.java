package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fast Crystal: cooldown serangan dianggap selalu penuh (1.0).
 *
 * Dulu mixin ini menarget ClientPlayerEntity, padahal getAttackCooldownProgress
 * dideklarasikan di PlayerEntity (ClientPlayerEntity cuma mewarisinya). Mixin nggak
 * bisa inject ke method turunan yang nggak dideklarasikan di kelas targetnya, jadi
 * dulu diam-diam nggak jalan (itu juga penyebab warning "Unable to determine
 * descriptor" di log build). Sekarang menarget PlayerEntity dan dibatasi ke pemain sendiri.
 */
@Mixin(PlayerEntity.class)
public abstract class AttackCooldownMixin {

        @Inject(method = "getAttackCooldownProgress", at = @At("HEAD"), cancellable = true, require = 0)
        private void n3xr$fastCrystal(float baseTime, CallbackInfoReturnable<Float> cir) {
                if (!N3XRConfig.fastCrystalEnabled) return;
                if ((Object) this != MinecraftClient.getInstance().player) return;
                cir.setReturnValue(1.0f);
        }
}
