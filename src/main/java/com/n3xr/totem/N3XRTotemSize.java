package com.n3xr.totem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Module "Totem Size": mengatur ukuran totem of undying di sisi client.
 *  - Pop size  : animasi totem yang melayang di layar saat totem kepakai
 *  - Main hand : totem yang dipegang di tangan utama (first person)
 *  - Off hand  : totem yang dipegang di tangan kedua (first person)
 * Semuanya dalam persen (10% sampai 200%, 100% = ukuran vanilla).
 *
 * Murni tampilan di client; tidak mengubah apa pun yang dikirim ke server.
 * Dibaca oleh mixin/TotemPopScaleMixin dan mixin/HeldTotemScaleMixin.
 * Disimpan di config/n3xr-totemsize.properties.
 */
public final class N3XRTotemSize {

        public static final int MIN = 10;
        public static final int MAX = 200;
        public static final int STEP = 10;

        private static boolean enabled = false;
        private static int popPercent = 50;      // contoh awal: animasi pop setengah ukuran
        private static int mainPercent = 100;
        private static int offPercent = 100;
        private static boolean loaded = false;

        private N3XRTotemSize() {}

        private static Path file() {
                return FabricLoader.getInstance().getConfigDir().resolve("n3xr-totemsize.properties");
        }

        private static int clamp(int v) {
                return Math.max(MIN, Math.min(MAX, v));
        }

        private static synchronized void load() {
                if (loaded) return;
                loaded = true;
                try {
                        Path f = file();
                        if (!Files.exists(f)) return;
                        Properties p = new Properties();
                        try (var in = Files.newBufferedReader(f)) { p.load(in); }
                        enabled = Boolean.parseBoolean(p.getProperty("enabled", "false"));
                        popPercent = clamp(Integer.parseInt(p.getProperty("pop", "50")));
                        mainPercent = clamp(Integer.parseInt(p.getProperty("main", "100")));
                        offPercent = clamp(Integer.parseInt(p.getProperty("off", "100")));
                } catch (Exception ignored) {}
        }

        public static synchronized void save() {
                try {
                        Properties p = new Properties();
                        p.setProperty("enabled", Boolean.toString(enabled));
                        p.setProperty("pop", Integer.toString(popPercent));
                        p.setProperty("main", Integer.toString(mainPercent));
                        p.setProperty("off", Integer.toString(offPercent));
                        try (var out = Files.newBufferedWriter(file())) { p.store(out, "N3XR totem size"); }
                } catch (IOException ignored) {}
        }

        public static boolean isEnabled() { load(); return enabled; }
        public static void setEnabled(boolean v) { load(); enabled = v; save(); }

        public static int getPop() { load(); return popPercent; }
        public static int getMain() { load(); return mainPercent; }
        public static int getOff() { load(); return offPercent; }

        public static void setPop(int v) { load(); popPercent = clamp(v); save(); }
        public static void setMain(int v) { load(); mainPercent = clamp(v); save(); }
        public static void setOff(int v) { load(); offPercent = clamp(v); save(); }

        public static void resetAll() {
                load();
                popPercent = 100;
                mainPercent = 100;
                offPercent = 100;
                save();
        }

        /** Pengali ukuran animasi pop (1.0 = vanilla). */
        public static float popScale() {
                return isEnabled() ? getPop() / 100f : 1f;
        }

        /** Pengali ukuran totem di tangan (1.0 = vanilla). */
        public static float heldScale(boolean mainHand) {
                if (!isEnabled()) return 1f;
                return (mainHand ? getMain() : getOff()) / 100f;
        }
}
