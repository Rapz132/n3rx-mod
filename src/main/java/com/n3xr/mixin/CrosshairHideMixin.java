package com.n3xr.mixin;

import com.n3xr.N3XRCrosshairHide;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sembunyikan crosshair vanilla (termasuk indikator serangan dan crosshair
 * debug F3) saat modul Crosshair custom N3XR nyala, supaya keduanya tidak
 * menumpuk. Keputusannya ada di N3XRCrosshairHide.
 */
@Mixin(InGameHud.class)
public class CrosshairHideMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void n3xr$hideVanillaCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (N3XRCrosshairHide.shouldHideVanilla()) {
            ci.cancel();
        }
    }
}
