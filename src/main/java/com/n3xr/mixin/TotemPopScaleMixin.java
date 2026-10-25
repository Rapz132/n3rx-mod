package com.n3xr.mixin;

import com.n3xr.totem.N3XRTotemSize;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Mengubah ukuran animasi totem yang melayang di layar saat totem kepakai.
 * Di renderFloatingItem(), ukuran ditentukan oleh satu pemanggilan
 * MatrixStack.scale(o, -o, o); kita kalikan ketiga argumennya dengan pengali
 * dari module Totem Size. Tanda minus di sumbu y (pembalik) ikut terkalikan,
 * jadi arah gambar tetap benar.
 */
@Mixin(GameRenderer.class)
public class TotemPopScaleMixin {

    @ModifyArgs(
            method = "renderFloatingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"),
            require = 0
    )
    private void n3xr$scaleTotemPop(Args args) {
        float factor = N3XRTotemSize.popScale();
        if (factor == 1.0f) return;
        float x = args.get(0);
        float y = args.get(1);
        float z = args.get(2);
        args.set(0, x * factor);
        args.set(1, y * factor);
        args.set(2, z * factor);
    }
}
