package com.n3xr.container;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;

/**
 * Menggambar isi container (3 baris x 9 kolom) di dalam tooltip item: kotak slot
 * bertint warna container, item, dan jumlahnya. Latar dan bingkai tooltip digambar
 * vanilla berdasarkan getWidth() dan getHeight().
 */
public class N3XRContainerTooltipComponent implements TooltipComponent {

        private static final int COLS = 9;
        private static final int ROWS = 3;
        private static final int CELL = 18;

        private final List<ItemStack> stacks;
        private final int color;

        public N3XRContainerTooltipComponent(N3XRContainerTooltipData data) {
                this.stacks = data.stacks();
                this.color = data.color();
        }

        @Override
        public int getHeight() {
                return ROWS * CELL + 4;
        }

        @Override
        public int getWidth(TextRenderer textRenderer) {
                return COLS * CELL + 2;
        }

        @Override
        public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
                int tint = (color & 0x00FFFFFF);
                for (int i = 0; i < COLS * ROWS; i++) {
                        int sx = x + (i % COLS) * CELL + 1;
                        int sy = y + (i / COLS) * CELL + 2;

                        // slot: latar gelap + garis tipis bertint warna container
                        context.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xA0000000 | tint);
                        context.fill(sx, sy, sx + 16, sy + 16, 0xFF1A1618);

                        if (i < stacks.size()) {
                                ItemStack s = stacks.get(i);
                                if (!s.isEmpty()) {
                                        context.drawItem(s, sx, sy);
                                        context.drawItemInSlot(textRenderer, s, sx, sy);
                                }
                        }
                }
        }
}
