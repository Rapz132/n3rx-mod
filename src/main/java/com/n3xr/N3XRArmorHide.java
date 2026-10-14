package com.n3xr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;

/**
 * Sembunyikan armor di badan player sendiri (helm, chestplate, leggings,
 * boots, sendiri-sendiri). Armor tetap terpasang dan tetap berfungsi,
 * cuma tidak digambar di layar kamu -- jadi hat/cosmetic nggak ketutupan.
 * Pemain lain tidak terpengaruh.
 *
 * Disimpan di config/n3xr-armorhide.txt, isinya nama slot yang disembunyikan
 * dipisah koma (contoh: "head,chest"). Dibaca oleh mixin/ArmorHideMixin.
 */
public final class N3XRArmorHide {

        public static final EquipmentSlot[] SLOTS = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        private static final Set<EquipmentSlot> HIDDEN = EnumSet.noneOf(EquipmentSlot.class);
        private static boolean loaded = false;

        private N3XRArmorHide() {}

        private static Path file() {
                return FabricLoader.getInstance().getConfigDir().resolve("n3xr-armorhide.txt");
        }

        private static void load() {
                if (loaded) return;
                loaded = true;
                try {
                        Path f = file();
                        if (!Files.exists(f)) return;
                        for (String part : Files.readString(f).trim().split(",")) {
                                String name = part.trim().toUpperCase(Locale.ROOT);
                                for (EquipmentSlot s : SLOTS) {
                                        if (s.name().equals(name)) HIDDEN.add(s);
                                }
                        }
                } catch (Exception ignored) {}
        }

        private static void save() {
                try {
                        StringBuilder sb = new StringBuilder();
                        for (EquipmentSlot s : SLOTS) {
                                if (HIDDEN.contains(s)) {
                                        if (sb.length() > 0) sb.append(',');
                                        sb.append(s.name().toLowerCase(Locale.ROOT));
                                }
                        }
                        Files.writeString(file(), sb.toString());
                } catch (Exception ignored) {}
        }

        public static synchronized boolean isHidden(EquipmentSlot slot) {
                load();
                return HIDDEN.contains(slot);
        }

        public static synchronized boolean allHidden() {
                load();
                for (EquipmentSlot s : SLOTS) if (!HIDDEN.contains(s)) return false;
                return true;
        }

        public static synchronized void toggle(EquipmentSlot slot) {
                load();
                if (!HIDDEN.remove(slot)) HIDDEN.add(slot);
                save();
        }

        /** hidden = true: sembunyikan semua; false: tampilkan semua. */
        public static synchronized void setAll(boolean hidden) {
                load();
                HIDDEN.clear();
                if (hidden) for (EquipmentSlot s : SLOTS) HIDDEN.add(s);
                save();
        }
}
