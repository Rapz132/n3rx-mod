package com.n3xr;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/**
 * Screen "Social" -- 2 tombol buka link YouTube & TikTok di browser
 * HP/PC (lewat Util.getOperatingSystem().open(...), cara resmi
 * vanilla buka link eksternal).
 */
public class N3XRSocialScreen extends Screen {

        private final Screen parent;

        public N3XRSocialScreen(Screen parent) {
                super(Text.literal("Social"));
                this.parent = parent;
        }

        @Override
        protected void init() {
                int w = 200, h = 20, gap = 8;
                int cx = this.width / 2;
                int y = this.height / 2 - 40;

                this.addDrawableChild(N3XRButton.of(cx - w / 2, y, w, h,
                        Text.literal("YouTube"),
                        b -> Util.getOperatingSystem().open("https://youtube.com/@x4cyzn")));
                y += h + gap;

                this.addDrawableChild(N3XRButton.of(cx - w / 2, y, w, h,
                        Text.literal("TikTok"),
                        b -> Util.getOperatingSystem().open("https://tiktok.com/@xchzn_")));
                y += h + gap * 2;

                this.addDrawableChild(N3XRButton.of(cx - w / 2, y, w, h,
                        Text.literal("Back"),
                        b -> this.client.setScreen(parent)));
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}
