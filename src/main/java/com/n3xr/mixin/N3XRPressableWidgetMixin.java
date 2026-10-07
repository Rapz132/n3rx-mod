package com.n3xr.mixin;

import com.n3xr.N3XRDraw;
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
                int bg = N3XRDraw.lerpColor(base, 0xE0B82C2C, n3xr$hover);
                N3XRDraw.fillRounded(context, x, y, x + w, y + h, bg, Math.min(8, h / 2));

                int textColor = this.active ? N3XRDraw.lerpColor(0xFFCCAAAA, 0xFFFFFFFF, n3xr$hover) : 0xFFA0A0A0;
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
}
