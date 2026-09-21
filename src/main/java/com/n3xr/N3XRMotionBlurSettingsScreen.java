package com.n3xr;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * Popup kecil buat ngatur seberapa kuat efek Motion Blur, dibuka
 * lewat tombol gear (\u2699) di sebelah toggle "Motion Blur" di
 * N3XRConfigScreen (mengikuti pola yang sama seperti Crosshair /
 * Real Time yang juga buka screen sendiri lewat gear-nya).
 */
public class N3XRMotionBlurSettingsScreen extends class_437 {

        private static final float MIN_STRENGTH = 0.05f;
        private static final float MAX_STRENGTH = 0.9f;
        private static final float STEP = 0.05f;

        private final class_437 parent;
        private class_2561 valueLabel;

        public N3XRMotionBlurSettingsScreen(class_437 parent) {
                super(class_2561.method_43470("Motion Blur"));
                this.parent = parent;
        }

        @Override
        protected void method_25426() {
                updateLabel();

                int w = 200, h = 20, gap = 8;
                int cx = this.field_22789 / 2;
                int y = this.field_22790 / 2 - 20;

                this.method_37063(N3XRButton.of(cx - w / 2, y, w, h,
                        class_2561.method_43470("Strength \u2212"),
                        b -> {
                                N3XRConfig.motionBlurStrength = Math.max(MIN_STRENGTH, N3XRConfig.motionBlurStrength - STEP);
                                updateLabel();
                        }));
                y += h + gap;

                this.method_37063(N3XRButton.of(cx - w / 2, y, w, h,
                        class_2561.method_43470("Strength +"),
                        b -> {
                                N3XRConfig.motionBlurStrength = Math.min(MAX_STRENGTH, N3XRConfig.motionBlurStrength + STEP);
                                updateLabel();
                        }));
                y += h + gap * 2;

                this.method_37063(N3XRButton.of(cx - w / 2, y, w, h,
                        class_2561.method_43470("Back"),
                        b -> this.field_22787.method_1507(parent)));
        }

        private void updateLabel() {
                valueLabel = class_2561.method_43470(String.format("Strength: %.2f", N3XRConfig.motionBlurStrength));
        }

        @Override
        public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
                super.method_25394(context, mouseX, mouseY, delta);

                int cx = this.field_22789 / 2;
                int labelY = this.field_22790 / 2 - 20 - 16;
                int tw = this.field_22793.method_27525(valueLabel);
                context.method_51439(this.field_22793, valueLabel, cx - tw / 2, labelY, 0xFFFFFFFF, true);
        }
}
