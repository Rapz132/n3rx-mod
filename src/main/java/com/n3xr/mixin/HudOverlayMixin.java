package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudOverlayMixin {

	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true, require = 0)
	private void n3xr$hideOverlay(net.minecraft.client.gui.DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
		String path = texture.getPath();
		if (N3XRConfig.noPumpkinOverlay && path.contains("pumpkinblur")) {
			ci.cancel();
		}
		if (N3XRConfig.noFireOverlay && (path.contains("fire") || path.contains("forcefield"))) {
			ci.cancel();
		}
	}
}