package com.n3xr.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nambahin tombol "N3XR YouTube" di bawah tombol Disconnect di
 * pause menu (GameMenuScreen), buka link YouTube N3XR di browser.
 *
 * Posisi Y di bawah ini perkiraan berdasarkan layout vanilla
 * standar (Back to Game -> 2 baris 2-kolom -> Disconnect), karena
 * nggak ada cara resmi "taruh persis di bawah widget X" tanpa
 * hitung ulang layout vanilla manual. Kemungkinan perlu disesuaikan
 * dikit abis lihat hasilnya in-game (kirim screenshot kalau
 * ketumpuk/ada gap aneh).
 */
@Mixin(GameMenuScreen.class)
public abstract class N3XRGameMenuScreenMixin extends Screen {

        protected N3XRGameMenuScreenMixin(Text title) {
                super(title);
        }

        @Inject(method = "init", at = @At("TAIL"), require = 0)
        private void n3xr$addYoutubeButton(CallbackInfo ci) {
                int buttonWidth = 204;
                int x = this.width / 2 - buttonWidth / 2;
                int y = this.height / 4 + 72 - 16 + 24 + 24 + 24 + 24 + 6;

                this.addDrawableChild(ButtonWidget.builder(
                                Text.literal("N3XR YouTube"),
                                b -> Util.getOperatingSystem().open("https://youtube.com/@x4cyzn")
                        )
                        .dimensions(x, y, buttonWidth, 20)
                        .build());
        }
}
