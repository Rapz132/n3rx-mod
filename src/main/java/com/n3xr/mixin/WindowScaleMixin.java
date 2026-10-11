package com.n3xr.mixin;

import com.n3xr.N3XRGuiScale;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Kalau GUI scale custom N3XR aktif (1-5), pakai itu langsung dan lewati
 * batas vanilla (yang membatasi scale berdasarkan resolusi window,
 * minimal 320x240 setelah dibagi scale).
 */
@Mixin(Window.class)
public class WindowScaleMixin {

    @Inject(method = "calculateScaleFactor", at = @At("HEAD"), cancellable = true)
    private void n3xr$customGuiScale(int guiScale, boolean forceUnicodeFont, CallbackInfoReturnable<Integer> cir) {
        int custom = N3XRGuiScale.get();
        if (custom > 0) {
            cir.setReturnValue(custom);
        }
    }
}
