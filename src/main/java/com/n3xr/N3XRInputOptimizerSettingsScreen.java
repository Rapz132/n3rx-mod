package com.n3xr;

import com.n3xr.input.N3XRInputOptimizer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Pengaturan Input Optimizer, dibuka dari ikon gear di kartu module. */
public class N3XRInputOptimizerSettingsScreen extends Screen {

        private final Screen parent;

        public N3XRInputOptimizerSettingsScreen(Screen parent) {
                super(Text.literal("Input Optimizer"));
                this.parent = parent;
        }

        private static String onOff(String name, boolean v) {
                return name + ": " + (v ? "ON" : "OFF");
        }

        @Override
        protected void init() {
                int w = 200, h = 20, step = 24;
                int x = this.width / 2 - w / 2;
                int y = Math.max(52, this.height / 2 - 40);

                this.addDrawableChild(N3XRButton.of(x, y, w, h, Text.literal(onOff("Instant Attack", N3XRInputOptimizer.isInstantAttack())), b -> {
                        N3XRInputOptimizer.toggleInstantAttack();
                        b.setMessage(Text.literal(onOff("Instant Attack", N3XRInputOptimizer.isInstantAttack())));
                }));
                y += step;
                this.addDrawableChild(N3XRButton.of(x, y, w, h, Text.literal(onOff("Instant Use (right click)", N3XRInputOptimizer.isInstantUse())), b -> {
                        N3XRInputOptimizer.toggleInstantUse();
                        b.setMessage(Text.literal(onOff("Instant Use (right click)", N3XRInputOptimizer.isInstantUse())));
                }));
                y += step;
                this.addDrawableChild(N3XRButton.of(x, y, w, h, Text.literal(onOff("Disable VSync", N3XRInputOptimizer.isDisableVsync())), b -> {
                        N3XRInputOptimizer.toggleDisableVsync();
                        b.setMessage(Text.literal(onOff("Disable VSync", N3XRInputOptimizer.isDisableVsync())));
                }));
                y += step + 6;
                this.addDrawableChild(N3XRButton.of(x, y, w, h, Text.literal("Back"), b -> this.client.setScreen(parent)));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("Input Optimizer").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, (this.width - tw) / 2, 14, 0xFFFF3333, true);

                String[] info = {
                        "Clicks run right when you press, not on the next game tick.",
                        "Saves up to 50 ms (about 25 ms on average).",
                        "Touch delay, server tick, and ping are not affected."
                };
                int y = 28;
                for (String line : info) {
                        int lw = this.textRenderer.getWidth(line);
                        context.drawText(this.textRenderer, line, (this.width - lw) / 2, y, 0xFF999999, false);
                        y += 10;
                }

                // peringatan di bawah tombol
                String[] warn = {
                        "Instant Attack/Use change WHEN your clicks are sent.",
                        "Some servers treat that as unfair. Check the rules first."
                };
                int wy = this.height / 2 + 62;
                for (String line : warn) {
                        int lw = this.textRenderer.getWidth(line);
                        context.drawText(this.textRenderer, line, (this.width - lw) / 2, wy, 0xFFFFAA55, false);
                        wy += 10;
                }
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}
