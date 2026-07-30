package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class AttackCooldownMixin {

	@Inject(method = "getAttackCooldownProgress", at = @At("HEAD"), cancellable = true, require = 0)
	private void n3xr$fastCrystal(float baseTime, CallbackInfoReturnable<Float> cir) {
		if (N3XRConfig.fastCrystalEnabled) {
			cir.setReturnValue(1.0f);
		}
	}
}