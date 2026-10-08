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
                c.getMatrices().push();
                c.getMatrices().translate(N3XRConfig.totemCountX, N3XRConfig.totemCountY, 0);
                c.getMatrices().scale(scale, scale, 1f);

                int tw = mc.textRenderer.getWidth(text);
                c.fill(-2, -2, 16 + 2 + tw + 2, 18, 0x90000000);
                c.drawItem(ICON, 0, 0);
                c.drawText(mc.textRenderer, Text.literal(text), 18, 4, N3XRConfig.totemCountColor, true);

                c.getMatrices().pop();
        }
}
