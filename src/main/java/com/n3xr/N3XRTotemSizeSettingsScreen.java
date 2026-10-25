package com.n3xr;

import com.n3xr.totem.N3XRTotemSize;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Pengaturan Totem Size (ikon gear di kartu module): tiga ukuran dengan tombol - dan +. */
public class N3XRTotemSizeSettingsScreen extends Screen {

        private final Screen parent;

        public N3XRTotemSizeSettingsScreen(Screen parent) {
                super(Text.literal("Totem Size"));
                this.parent = parent;
        }

        private static final String[] LABELS = {"Pop animation", "Main hand", "Off hand"};

        private int rowY(int i) {
                return Math.max(46, this.height / 2 - 50) + i * 28;
        }

        private int rowX() {
                return this.width / 2 - 100;
        }

        @Override
        protected void init() {
                IntSupplier[] get = {N3XRTotemSize::getPop, N3XRTotemSize::getMain, N3XRTotemSize::getOff};
                IntConsumer[] set = {N3XRTotemSize::setPop, N3XRTotemSize::setMain, N3XRTotemSize::setOff};

                for (int i = 0; i < 3; i++) {
                        final int idx = i;
                        int y = rowY(i);
                        int x = rowX();

                        // - [nilai] +   (klik nilai = kembali ke 100%)
                        this.addDrawableChild(N3XRButton.of(x + 100, y, 22, 20, Text.literal("-"), b -> {
                                set[idx].accept(get[idx].getAsInt() - N3XRTotemSize.STEP);
                                this.clearAndInit();
                        }));
                        this.addDrawableChild(N3XRButton.of(x + 126, y, 48, 20, Text.literal(get[idx].getAsInt() + "%"), b -> {
                                set[idx].accept(100);
                                this.clearAndInit();
                        }));
                        this.addDrawableChild(N3XRButton.of(x + 178, y, 22, 20, Text.literal("+"), b -> {
                                set[idx].accept(get[idx].getAsInt() + N3XRTotemSize.STEP);
                                this.clearAndInit();
                        }));
                }

                int by = rowY(3) + 6;
                this.addDrawableChild(N3XRButton.of(rowX(), by, 96, 20, Text.literal("Reset all"), b -> {
                        N3XRTotemSize.resetAll();
                        this.clearAndInit();
                }));
                this.addDrawableChild(N3XRButton.of(rowX() + 104, by, 96, 20, Text.literal("Back"),
                        b -> this.client.setScreen(parent)));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("Totem Size").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, (this.width - tw) / 2, 14, 0xFFFF3333, true);

                String hint = "100% = vanilla size. Click the value to reset it.";
                int hw = this.textRenderer.getWidth(hint);
                context.drawText(this.textRenderer, hint, (this.width - hw) / 2, 28, 0xFF999999, false);

                for (int i = 0; i < 3; i++) {
                        context.drawText(this.textRenderer, LABELS[i], rowX(), rowY(i) + 6, 0xFFFFFFFF, true);
                }
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}
