package com.n3xr.mixin;

import com.n3xr.N3XRConfigScreen;
import com.n3xr.N3XRCosmeticsScreen;
import com.n3xr.N3XRFastServerScreen;
import com.n3xr.N3XRSocialScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Panel tombol N3XR di title screen, custom-draw sepenuhnya (bukan
 * pakai N3XRButton bawaan yang gayanya kaku/kotak-kotak) -- dibikin
 * konsisten sama gaya "smooth" yang udah dipakai di screen lain
 * (rounded corner + hover yang di-lerp halus, bukan garis statis).
 * Ukuran juga dikecilin dari versi sebelumnya.
 */
@Mixin(TitleScreen.class)
public abstract class N3XRTitleScreenMixin extends Screen {

        protected N3XRTitleScreenMixin(Text title) {
                super(title);
        }

        private static final int PANEL_W = 92;
        private static final int ROW_H = 18;
        private static final int GAP = 3;
        private static final int PAD = 6;

        private final String[] n3xr$labels = {"N3XR Client", "Fast Server", "Social", "Cosmetics", "Settings"};
        private final float[] n3xr$hover = new float[5];
        private int n3xr$panelX1, n3xr$panelY1, n3xr$panelX2, n3xr$panelY2;
        private int[][] n3xr$rowRects;
        private long n3xr$lastRenderNanos = 0;

        @Inject(method = "init", at = @At("TAIL"), require = 0)
        private void n3xr$layoutPanel(CallbackInfo ci) {
                n3xr$panelX1 = 10;
                n3xr$panelY1 = this.height / 2 - (ROW_H * 5 + GAP * 4 + PAD * 2) / 2;
                n3xr$panelX2 = n3xr$panelX1 + PANEL_W;

                n3xr$rowRects = new int[5][4];
                int y = n3xr$panelY1 + PAD;
                for (int i = 0; i < 5; i++) {
                        n3xr$rowRects[i] = new int[]{n3xr$panelX1 + 4, y, PANEL_W - 8, ROW_H};
                        y += ROW_H + GAP;
                }
                n3xr$panelY2 = y - GAP + PAD;
        }

        @Inject(method = "render", at = @At("TAIL"), require = 0)
        private void n3xr$renderPanel(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
                if (n3xr$rowRects == null) return;

                long now = System.nanoTime();
                float dt = n3xr$lastRenderNanos == 0 ? 0f : (now - n3xr$lastRenderNanos) / 1_000_000_000f;
                dt = Math.min(dt, 0.1f);
                n3xr$lastRenderNanos = now;

                n3xr$fillRounded(context, n3xr$panelX1, n3xr$panelY1, n3xr$panelX2, n3xr$panelY2, 0xC00A0505, 5);

                for (int i = 0; i < n3xr$rowRects.length; i++) {
                        int[] r = n3xr$rowRects[i];
                        boolean hovered = mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3];

                        float target = hovered ? 1f : 0f;
                        n3xr$hover[i] += (target - n3xr$hover[i]) * Math.min(1f, dt * 12f);

                        int bg = n3xr$lerpColor(0x000A0505, 0x90B82C2C, n3xr$hover[i]);
                        n3xr$fillRounded(context, r[0], r[1], r[0] + r[2], r[1] + r[3], bg, 3);

                        int textColor = n3xr$lerpColor(0xFFCCAAAA, 0xFFFFFFFF, n3xr$hover[i]);
                        int tw = this.textRenderer.getWidth(n3xr$labels[i]);
                        context.drawText(this.textRenderer, n3xr$labels[i],
                                r[0] + (r[2] - tw) / 2, r[1] + (r[3] - 8) / 2, textColor, false);
                }
        }

        @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
        private void n3xr$onPanelClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
                if (n3xr$rowRects == null) return;

                for (int i = 0; i < n3xr$rowRects.length; i++) {
                        int[] r = n3xr$rowRects[i];
                        if (mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                                switch (i) {
                                        case 0 -> this.client.setScreen(new N3XRConfigScreen());
                                        case 1 -> this.client.setScreen(new N3XRFastServerScreen(this));
                                        case 2 -> this.client.setScreen(new N3XRSocialScreen(this));
                                        case 3 -> this.client.setScreen(new N3XRCosmeticsScreen());
                                        case 4 -> this.client.setScreen(new OptionsScreen(this, this.client.options));
                                }
                                cir.setReturnValue(true);
                                return;
                        }
                }
        }

        private void n3xr$fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
                radius = Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
                if (radius <= 0) { context.fill(x1, y1, x2, y2, color); return; }
                context.fill(x1 + radius, y1, x2 - radius, y2, color);
                context.fill(x1, y1 + radius, x1 + radius, y2 - radius, color);
                context.fill(x2 - radius, y1 + radius, x2, y2 - radius, color);
                for (int i = 0; i < radius; i++) {
                        int dx = radius - (int) Math.sqrt(Math.max(0, radius * radius - (radius - i) * (radius - i)));
                        context.fill(x1 + dx, y1 + i, x1 + radius, y1 + i + 1, color);
                        context.fill(x2 - radius, y1 + i, x2 - dx, y1 + i + 1, color);
                        context.fill(x1 + dx, y2 - i - 1, x1 + radius, y2 - i, color);
                        context.fill(x2 - radius, y2 - i - 1, x2 - dx, y2 - i, color);
                }
        }

        private int n3xr$lerpColor(int colorA, int colorB, float t) {
                t = Math.max(0f, Math.min(1f, t));
                int aA = (colorA >> 24) & 0xFF, rA = (colorA >> 16) & 0xFF, gA = (colorA >> 8) & 0xFF, bA = colorA & 0xFF;
                int aB = (colorB >> 24) & 0xFF, rB = (colorB >> 16) & 0xFF, gB = (colorB >> 8) & 0xFF, bB = colorB & 0xFF;
                int a = (int) (aA + (aB - aA) * t);
                int r = (int) (rA + (rB - rA) * t);
                int g = (int) (gA + (gB - gA) * t);
                int b = (int) (bA + (bB - bA) * t);
                return (a << 24) | (r << 16) | (g << 8) | b;
        }
}
