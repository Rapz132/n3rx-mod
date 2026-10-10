package com.n3xr.totemcount;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * Module Totem Count (HUD):  [icon totem] : [jumlah totem]
 * Jumlah = semua Totem of Undying di inventory (hotbar + tas) + offhand.
 *
 * Posisi otomatis ditahan di dalam layar. Koordinat HUD itu dalam satuan "GUI scale",
 * jadi posisi yang tersimpan dari layar lain (atau default yang kebesaran) bisa jatuh
 * di luar layar dan modulnya kelihatan "hilang". Kalau itu terjadi, posisinya dibetulkan
 * dan disimpan balik ke config, jadi HUD Editor juga menampilkannya di tempat yang bisa dipegang.
 */
public final class N3XRTotemCount {

        private N3XRTotemCount() {}

        private static final ItemStack ICON = new ItemStack(Items.TOTEM_OF_UNDYING);

        public static int countTotems(MinecraftClient mc) {
                if (mc.player == null) return 0;
                PlayerInventory inv = mc.player.getInventory();
                int count = 0;
                for (ItemStack s : inv.main) {
                        if (s.isOf(Items.TOTEM_OF_UNDYING)) count += s.getCount();
                }
                for (ItemStack s : inv.offHand) {
                        if (s.isOf(Items.TOTEM_OF_UNDYING)) count += s.getCount();
                }
                return count;
        }

        public static void render(DrawContext c, MinecraftClient mc) {
                String text = ": " + countTotems(mc);

                float scale = N3XRConfig.getScale("TotemCount");
                int tw = mc.textRenderer.getWidth(text);

                // ukuran kotak di layar (satuan GUI) = (2 + 16 + 2 + teks + 2) * scale, tinggi 20 * scale
                float boxW = (20 + tw + 2) * scale;
                float boxH = 20 * scale;
                int sw = mc.getWindow().getScaledWidth();
                int sh = mc.getWindow().getScaledHeight();

                int minX = (int) Math.ceil(2 * scale), minY = (int) Math.ceil(2 * scale);
                int maxX = Math.max(minX, (int) (sw - boxW));
                int maxY = Math.max(minY, (int) (sh - boxH));
                int x = Math.max(minX, Math.min(N3XRConfig.totemCountX, maxX));
                int y = Math.max(minY, Math.min(N3XRConfig.totemCountY, maxY));
                if (x != N3XRConfig.totemCountX) N3XRConfig.totemCountX = x;
                if (y != N3XRConfig.totemCountY) N3XRConfig.totemCountY = y;

                c.getMatrices().push();
                c.getMatrices().translate(x, y, 0);
                c.getMatrices().scale(scale, scale, 1f);

                c.fill(-2, -2, 16 + 2 + tw + 2, 18, 0x90000000);
                c.drawItem(ICON, 0, 0);
                c.drawText(mc.textRenderer, Text.literal(text), 18, 4, N3XRConfig.totemCountColor, true);

                c.getMatrices().pop();
        }
}
