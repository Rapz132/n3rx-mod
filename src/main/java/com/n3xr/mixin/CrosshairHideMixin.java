package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sembunyikan crosshair vanilla saat crosshair custom aktif, supaya
 * keduanya tidak menumpuk. Kalau custom crosshair dinyalakan tapi
 * gridnya masih kosong, crosshair vanilla tetap tampil (biar nggak
 * sampai tanpa crosshair sama sekali).
 */
@Mixin(InGameHud.class)
public class CrosshairHideMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void n3xr$hideVanillaCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (N3XRConfig.customCrosshairEnabled && n3xr$hasPixels()) {
            ci.cancel();
        }
    }

    private static boolean n3xr$hasPixels() {
        for (int p : N3XRConfig.crosshairPixels) {
            if (p != 0) return true;
        }
        return false;
    }
}
