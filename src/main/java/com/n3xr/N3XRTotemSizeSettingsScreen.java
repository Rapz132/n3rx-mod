package com.n3xr;

import com.n3xr.totem.N3XRTotemSize;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * Pengaturan Totem Size (ikon gear di kartu module): tiga ukuran dengan tombol - dan +,
 * tombol "Test pop" untuk melihat animasi pop kapan saja, dan status hook (diagnosa)
 * yang menunjukkan apakah mixin-nya benar-benar jalan di versi Minecraft kamu.
 */
public class N3XRTotemSizeSettingsScreen extends Screen {

        private final Screen parent;

        public N3XRTotemSizeSettingsScreen(Screen parent) {
                super(Text.literal("Totem Size"));
                this.parent = parent;
        }

        private static final String[] LABELS = {"Pop animation", "Main hand", "Off hand"};

        private int top() {
                return Math.max(40, this.height / 2 - 70);
        }

        private int rowY(int i) {
                return top() + i * 26;
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

                int by = rowY(3) + 4;
                this.addDrawableChild(N3XRButton.of(rowX(), by, 96, 20, Text.literal("Reset all"), b -> {
                        N3XRTotemSize.resetAll();
                        this.clearAndInit();
                }));
                // tutup layar lalu tampilkan animasi pop totem, supaya ukurannya langsung kelihatan
                this.addDrawableChild(N3XRButton.of(rowX() + 104, by, 96, 20, Text.literal("Test pop"), b -> {
                        MinecraftClient mc = this.client;
                        mc.setScreen(null);
                        mc.gameRenderer.showFloatingItem(new ItemStack(Items.TOTEM_OF_UNDYING));
                }));
                this.addDrawableChild(N3XRButton.of(rowX(), by + 24, 200, 20, Text.literal("Back"),
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
                context.drawText(this.textRenderer, hint, (this.width - hw) / 2, 27, 0xFF999999, false);

                for (int i = 0; i < 3; i++) {
                        context.drawText(this.textRenderer, LABELS[i], rowX(), rowY(i) + 6, 0xFFFFFFFF, true);
                }

                // status hook (diagnosa)
                int sy = rowY(3) + 4 + 24 + 28;
                boolean enabled = N3XRTotemSize.isEnabled();
                String state = enabled ? "Module: ON" : "Module: OFF (turn it on in the module list)";
                context.drawText(this.textRenderer, state, rowX(), sy, enabled ? 0xFF55FF55 : 0xFFFF5555, false);
                sy += 11;
                context.drawText(this.textRenderer,
                        N3XRTotemSize.popHookSeen ? "Pop hook: working" : "Pop hook: not seen yet (press Test pop)",
                        rowX(), sy, N3XRTotemSize.popHookSeen ? 0xFF55FF55 : 0xFFAAAAAA, false);
                sy += 11;
                context.drawText(this.textRenderer,
                        N3XRTotemSize.heldHookSeen ? "Held hook: working" : "Held hook: not seen yet (hold a totem)",
                        rowX(), sy, N3XRTotemSize.heldHookSeen ? 0xFF55FF55 : 0xFFAAAAAA, false);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}
