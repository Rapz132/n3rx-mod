package com.n3xr.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ganti tampilan SEMUA tombol bawaan Minecraft (di semua screen:
 * title, pause, options, dll., termasuk tombol on/off yang ganti
 * nilai) jadi gelap + sudut melengkung, satu gaya dengan panel N3XR.
 *
 * Yang nggak ikut berubah: slider, checkbox, dan tombol icon, karena
 * widget-widget itu punya render sendiri dan nggak lewat PressableWidget.
 */
@Mixin(PressableWidget.class)
public abstract class N3XRPressableWidgetMixin extends ClickableWidget {

        public N3XRPressableWidgetMixin(int x, int y, int width, int height, Text message) {
                super(x, y, width, height, message);
        }

        private float n3xr$hover = 0f;
        private long n3xr$lastNanos = 0L;

        @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true, require = 0)
        private void n3xr$renderRounded(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
                MinecraftClient mc = MinecraftClient.getInstance();

                long now = System.nanoTime();
                float dt = n3xr$lastNanos == 0L ? 0f : Math.min((now - n3xr$lastNanos) / 1_000_000_000f, 0.1f);
                n3xr$lastNanos = now;

                boolean hot = this.active && (this.isHovered() || this.isFocused());
                n3xr$hover += ((hot ? 1f : 0f) - n3xr$hover) * Math.min(1f, dt * 12f);

                int x = this.getX(), y = this.getY(), w = this.getWidth(), h = this.getHeight();
                int base = this.active ? 0xC00A0505 : 0x80000000;
                int bg = n3xr$lerpColor(base, 0xE0B82C2C, n3xr$hover);
                n3xr$fillRounded(context, x, y, x + w, y + h, bg, Math.min(8, h / 2));

                int textColor = this.active ? n3xr$lerpColor(0xFFCCAAAA, 0xFFFFFFFF, n3xr$hover) : 0xFFA0A0A0;
                int textY = y + (h - 8) / 2;
                Text msg = this.getMessage();

                if (mc.textRenderer.getWidth(msg) <= w - 8) {
                        context.drawCenteredTextWithShadow(mc.textRenderer, msg, x + w / 2, textY, textColor);
                } else {
                        // label kepanjangan: dipotong supaya nggak keluar dari tombol
                        String cut = mc.textRenderer.trimToWidth(msg.getString(), w - 8);
                        context.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(cut), x + w / 2, textY, textColor);
                }

                ci.cancel();
        }

        private static void n3xr$fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
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

        private static int n3xr$lerpColor(int colorA, int colorB, float t) {
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
