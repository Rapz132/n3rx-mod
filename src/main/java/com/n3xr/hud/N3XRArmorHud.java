package com.n3xr.hud;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Armor HUD (icon + % durability + item tangan kiri) dan Potions HUD (icon efek + nama + durasi).
 * Target: Minecraft 1.21 (Yarn mappings).
 *
 * Armor HUD:
 *  - helm -> chestplate -> leggings -> boots (dari atas ke bawah)
 *  - tiap armor ada % durability; warna ikut armorColor, jadi merah kalau <= 20%,
 *    dan kedip merah/putih kalau <= 10% (hampir hancur)
 *  - baris terpisah di bawah armor: item di offhand (tangan kiri / tombol F) = icon + "24x"
 *  - background bisa dimatikan lewat N3XRConfig.armorBackground
 */
public final class N3XRArmorHud {

    private N3XRArmorHud() {}

    private static final int ROW_H = 20;       // jarak antar baris armor
    private static final int LOW_PERCENT = 20; // <= ini jadi merah
    private static final int CRIT_PERCENT = 10; // <= ini kedip
    private static final int RED = 0xFF5555;

    public static void renderArmor(DrawContext c, MinecraftClient mc) {
        PlayerEntity p = mc.player;
        if (p == null) return;
        TextRenderer tr = mc.textRenderer;
        float scale = N3XRConfig.getScale("Armor");

        ItemStack offhand = p.getOffHandStack();
        boolean showOffhand = !offhand.isEmpty();

        // lebar teks terpanjang yang mungkin muncul, biar background nggak berubah-ubah ukurannya
        int textW = tr.getWidth("100%");
        if (showOffhand) textW = Math.max(textW, tr.getWidth(offhand.getCount() + "x"));
        int width = 16 + 4 + textW;

        int armorBottom = 3 * ROW_H + 16;             // bawah icon boots
        int offY = 4 * ROW_H + 4;                     // baris offhand (ada jarak kecil dari armor)
        int bottom = showOffhand ? offY + 16 : armorBottom;

        c.getMatrices().push();
        c.getMatrices().translate(N3XRConfig.armorX, N3XRConfig.armorY, 0);
        c.getMatrices().scale(scale, scale, 1f);

        if (N3XRConfig.armorBackground) {
            c.fill(-2, -2, width + 2, bottom + 2, 0x90000000);
        }

        EquipmentSlot[] order = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
        int y = 0;
        for (EquipmentSlot slot : order) {
            ItemStack stack = p.getEquippedStack(slot);
            if (stack.isEmpty()) {
                c.fill(0, y, 16, y + 16, 0x40FFFFFF);
            } else {
                c.drawItem(stack, 0, y);
                c.drawItemInSlot(tr, stack, 0, y);
                if (stack.isDamageable() && stack.getMaxDamage() > 0) {
                    int max = stack.getMaxDamage();
                    int pct = Math.max(0, Math.min(100, (max - stack.getDamage()) * 100 / max));
                    c.drawText(tr, pct + "%", 20, y + 4, durabilityColor(pct), true);
                }
            }
            y += ROW_H;
        }

        if (showOffhand) {
            if (N3XRConfig.armorBackground) {
                c.fill(0, offY - 3, width, offY - 2, 0x40FFFFFF); // garis pemisah tipis
            }
            c.drawItem(offhand, 0, offY);
            c.drawText(tr, offhand.getCount() + "x", 20, offY + 4, N3XRConfig.armorColor, true);
        }

        c.getMatrices().pop();
    }

    private static int durabilityColor(int pct) {
        if (pct <= CRIT_PERCENT) {
            boolean on = (System.currentTimeMillis() / 250) % 2 == 0;
            return on ? RED : 0xFFFFFF;
        }
        if (pct <= LOW_PERCENT) return RED;
        return N3XRConfig.armorColor;
    }

    // ------------------------------------------------------------------ Potions

    /** Tinggi satu baris potion (satuan GUI, sebelum scale). Samakan dengan N3XRHudLayout.rect(). */
    public static final int POTION_ROW_H = 14;

    public static void renderPotions(DrawContext c, MinecraftClient mc) {
        PlayerEntity p = mc.player;
        if (p == null) return;
        TextRenderer tr = mc.textRenderer;
        float scale = N3XRConfig.getScale("Potions");

        c.getMatrices().push();
        c.getMatrices().translate(N3XRConfig.potionsX, N3XRConfig.potionsY, 0);
        c.getMatrices().scale(scale, scale, 1f);

        int y = 0;
        for (StatusEffectInstance effect : p.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            String time = StatusEffectUtil.getDurationText(effect, 1.0f, 20.0f).getString();
            String text = name + " " + time;
            int tw = tr.getWidth(text);

            if (N3XRConfig.potionsBackground) {
                c.fill(-2, y - 2, 15 + tw + 2, y + 12, 0x90000000);
            }

            Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            c.drawSprite(0, y - 1, 0, 12, 12, sprite);
            c.drawText(tr, text, 15, y + 2, N3XRConfig.potionsColor, true);

            y += POTION_ROW_H;
        }

        c.getMatrices().pop();
    }
}
