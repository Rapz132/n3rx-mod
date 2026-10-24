package com.n3xr.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

/**
 * Module "Input Optimizer": memangkas jeda dari saat tombol ditekan sampai aksinya
 * dikirim ke server.
 *
 * Kenapa ada jeda: Minecraft baru memproses klik di client tick berikutnya (tiap
 * 50 ms), jadi klik yang masuk tepat setelah tick harus menunggu sampai 50 ms
 * (rata-rata sekitar 25 ms) sebelum serangan/aksi dikirim. "Instant clicks" menjalankan
 * serangan/aksi langsung saat tombol ditekan (lihat mixin/MouseInstantClickMixin),
 * jadi bagian menunggu tick itu hilang.
 *
 * Yang TIDAK bisa diubah modul ini: jeda sentuhan/input di Android atau Pojav,
 * tick server (50 ms), dan ping jaringan.
 *
 * PERINGATAN: Instant Attack dan Instant Use mengubah KAPAN klik dikirim ke server,
 * dan sebagian server menganggap itu keuntungan tidak adil atau tidak sesuai aturan
 * mereka. Karena itu keduanya MATI secara default; nyalakan hanya kalau aturan server
 * mengizinkan.
 *
 * Opsi: Instant Attack, Instant Use, dan Disable VSync (mengurangi jeda frame;
 * dikembalikan saat module dimatikan). Disimpan di config/n3xr-inputopt.properties.
 */
public final class N3XRInputOptimizer {

        private static boolean enabled = false;
        private static boolean instantAttack = false;
        private static boolean instantUse = false;
        private static boolean disableVsync = false;
        private static boolean loaded = false;

        // pengelolaan VSync
        private static boolean vsyncForced = false;
        private static boolean originalVsync = true;

        private N3XRInputOptimizer() {}

        private static Path file() {
                return FabricLoader.getInstance().getConfigDir().resolve("n3xr-inputopt.properties");
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
                        instantAttack = Boolean.parseBoolean(p.getProperty("instant_attack", "false"));
                        instantUse = Boolean.parseBoolean(p.getProperty("instant_use", "false"));
                        disableVsync = Boolean.parseBoolean(p.getProperty("disable_vsync", "false"));
                } catch (Exception ignored) {}
        }

        public static synchronized void save() {
                try {
                        Properties p = new Properties();
                        p.setProperty("enabled", Boolean.toString(enabled));
                        p.setProperty("instant_attack", Boolean.toString(instantAttack));
                        p.setProperty("instant_use", Boolean.toString(instantUse));
                        p.setProperty("disable_vsync", Boolean.toString(disableVsync));
                        try (var out = Files.newBufferedWriter(file())) { p.store(out, "N3XR input optimizer"); }
                } catch (IOException ignored) {}
        }

        public static boolean isEnabled() { load(); return enabled; }
        public static void setEnabled(boolean v) { load(); enabled = v; save(); }

        public static boolean isInstantAttack() { load(); return instantAttack; }
        public static boolean isInstantUse() { load(); return instantUse; }
        public static boolean isDisableVsync() { load(); return disableVsync; }

        public static void toggleInstantAttack() { load(); instantAttack = !instantAttack; save(); }
        public static void toggleInstantUse() { load(); instantUse = !instantUse; save(); }
        public static void toggleDisableVsync() { load(); disableVsync = !disableVsync; save(); }

        /** Dipanggil tiap client tick: menyalakan/mengembalikan VSync sesuai pengaturan. */
        public static void tick(MinecraftClient mc) {
                if (mc.options == null) return;
                boolean want = isEnabled() && isDisableVsync();
                if (want && !vsyncForced) {
                        originalVsync = mc.options.getEnableVsync().getValue();
                        if (originalVsync) mc.options.getEnableVsync().setValue(false);
                        vsyncForced = true;
                } else if (!want && vsyncForced) {
                        if (originalVsync) mc.options.getEnableVsync().setValue(true);
                        vsyncForced = false;
                }
        }
}
