package com.n3xr;

import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

/**
 * Popup kecil buat ngatur seberapa kuat efek Motion Blur, dibuka
 * lewat tombol gear (⚙) di sebelah toggle "Motion Blur" di
 * N3XRConfigScreen (mengikuti pola yang sama seperti Crosshair /
 * Real Time yang juga buka screen sendiri lewat gear-nya).
 */
public class N3XRMotionBlurSettingsScreen extends Screen {

        private static final float MIN_STRENGTH = 0.05f;
        private static final float MAX_STRENGTH = 0.9f;
        private static final float STEP = 0.05f;

        private final Screen parent;
        private Text valueLabel;

        public N3XRMotionBlurSettingsScreen(Screen parent) {
                super(Text.literal("Motion Blur"));
                this.parent = parent;
        }

        @Override
        protected void init() {
                updateLabel();

                int w = 200, h = 20, gap = 8;
                int cx = this.width / 2;
                int y = this.height / 2 - 20;

                this.addDrawableChild(N3XRButton.of(cx - w / 2, y, w, h,
                        Text.literal("Strength −"),
                        b -> {
                                N3XRConfig.motionBlurStrength = Math.max(MIN_STRENGTH, N3XRConfig.motionBlurStrength - STEP);
                                updateLabel();
                        }));
                y += h + gap;

                this.addDrawableChild(N3XRButton.of(cx - w / 2, y, w, h,
                        Text.literal("Strength +"),
                        b -> {
                                N3XRConfig.motionBlurStrength = Math.min(MAX_STRENGTH, N3XRConfig.motionBlurStrength + STEP);
                                updateLabel();
                        }));
                y += h + gap * 2;

                this.addDrawableChild(N3XRButton.of(cx - w / 2, y, w, h,
                        Text.literal("Back"),
                        b -> this.client.setScreen(parent)));
        }

        private void updateLabel() {
                valueLabel = Text.literal(String.format("Strength: %.2f", N3XRConfig.motionBlurStrength));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                int cx = this.width / 2;
                int labelY = this.height / 2 - 20 - 16;
                int tw = this.textRenderer.getWidth(valueLabel);
                context.drawText(this.textRenderer, valueLabel, cx - tw / 2, labelY, 0xFFFFFFFF, true);
        }
}
