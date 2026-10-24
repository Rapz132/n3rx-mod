package com.n3xr.mixin;

import com.n3xr.input.N3XRInputOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Input Optimizer / Instant clicks.
 *
 * Vanilla hanya menandai klik di KeyBinding saat tombol ditekan, lalu baru
 * menjalankan serangan/aksinya di client tick berikutnya (sampai 50 ms kemudian).
 * Di sini, begitu vanilla selesai memproses event tombol mouse, kita langsung
 * menjalankan doAttack()/doItemUse() dan "menghabiskan" penanda klik
 * (wasPressed) supaya tick berikutnya tidak menjalankannya lagi (tidak double).
 *
 * Aturan sama dengan vanilla: tidak dijalankan kalau ada layar terbuka, game
 * dijeda, kursor tidak terkunci, atau pemain sedang memakai item (makan, dll).
 * Menahan tombol (mining / serang terus) tetap ditangani vanilla.
 */
@Mixin(Mouse.class)
public abstract class MouseInstantClickMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract boolean isCursorLocked();

    @Inject(method = "onMouseButton", at = @At("RETURN"), require = 0)
    private void n3xr$instantClick(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action != 1) return;                                   // hanya saat ditekan
        if (!N3XRInputOptimizer.isEnabled()) return;

        MinecraftClient mc = this.client;
        if (mc.currentScreen != null || mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.isPaused() || !this.isCursorLocked()) return;
        if (mc.player.isUsingItem()) return;

        MinecraftClientInvoker invoker = (MinecraftClientInvoker) mc;

        if (N3XRInputOptimizer.isInstantAttack() && mc.options.attackKey.matchesMouse(button)) {
            if (mc.options.attackKey.wasPressed()) invoker.n3xr$doAttack();
        } else if (N3XRInputOptimizer.isInstantUse() && mc.options.useKey.matchesMouse(button)) {
            if (mc.options.useKey.wasPressed()) invoker.n3xr$doItemUse();
        }
    }
}
