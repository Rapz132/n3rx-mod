package com.n3xr.mixin;

import com.n3xr.N3XRButton;
import com.n3xr.N3XRConfigScreen;
import com.n3xr.N3XRCosmeticsScreen;
import com.n3xr.N3XRFastServerScreen;
import com.n3xr.N3XRSocialScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nambahin panel tombol N3XR di title screen (menu utama Minecraft),
 * posisinya di sisi kiri layar (nggak nabrak tombol Singleplayer/
 * Multiplayer vanilla yang di tengah):
 *   - N3XR Client -> buka N3XRConfigScreen (list module)
 *   - Fast Server -> buka N3XRFastServerScreen (quick-connect)
 *   - Social      -> buka N3XRSocialScreen (YouTube/TikTok)
 *   - Cosmetics   -> buka N3XRCosmeticsScreen (cape/hat)
 *   - Settings    -> buka OptionsScreen vanilla biasa
 *
 * Class ini extends Screen (sama kayak TitleScreen aslinya) supaya
 * bisa manggil this.client / this.addDrawableChild(...) langsung
 * tanpa perlu @Shadow -- pattern-nya jauh lebih simpel dari mixin
 * Cape/Hat (yang butuh generic khusus), karena Screen bukan class
 * generic dan konstruktornya simpel.
 */
@Mixin(TitleScreen.class)
public abstract class N3XRTitleScreenMixin extends Screen {

        protected N3XRTitleScreenMixin(Text title) {
                super(title);
        }

        @Inject(method = "init", at = @At("TAIL"), require = 0)
        private void n3xr$addPanel(CallbackInfo ci) {
                int x = 10;
                int y = this.height / 2 - 60;
                int w = 110, h = 20, gap = 4;

                this.addDrawableChild(N3XRButton.of(x, y, w, h,
                        Text.literal("N3XR Client"),
                        b -> this.client.setScreen(new N3XRConfigScreen())));
                y += h + gap;

                this.addDrawableChild(N3XRButton.of(x, y, w, h,
                        Text.literal("Fast Server"),
                        b -> this.client.setScreen(new N3XRFastServerScreen(this))));
                y += h + gap;

                this.addDrawableChild(N3XRButton.of(x, y, w, h,
                        Text.literal("Social"),
                        b -> this.client.setScreen(new N3XRSocialScreen(this))));
                y += h + gap;

                this.addDrawableChild(N3XRButton.of(x, y, w, h,
                        Text.literal("Cosmetics"),
                        b -> this.client.setScreen(new N3XRCosmeticsScreen())));
                y += h + gap;

                this.addDrawableChild(N3XRButton.of(x, y, w, h,
                        Text.literal("Settings"),
                        b -> this.client.setScreen(new OptionsScreen(this, this.client.options))));
        }
}
