package com.n3xr;

import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

/**
 * GUI scale custom 1-5 yang tidak dibatasi ukuran window seperti slider
 * vanilla (vanilla membatasi scale maksimum berdasarkan resolusi, makanya
 * di HP mentok di 3).
 *
 * 0 = pakai pengaturan vanilla (Auto / slider Video Settings).
 * 1..5 = paksa scale itu lewat WindowScaleMixin.
 *
 * Disimpan di config/n3xr-guiscale.txt, terpisah dari N3XRConfigStorage
 * supaya bisa dibaca sangat awal (Window dihitung sebelum mod init).
 */
public final class N3XRGuiScale {

        public static final int MAX = 5;

        private static int value = 0;
        private static boolean loaded = false;

        private N3XRGuiScale() {}

        private static Path file() {
                return FabricLoader.getInstance().getConfigDir().resolve("n3xr-guiscale.txt");
        }

        private static int clamp(int v) {
                return Math.max(0, Math.min(MAX, v));
        }

        public static synchronized int get() {
                if (!loaded) {
                        loaded = true;
                        try {
                                Path f = file();
                                if (Files.exists(f)) value = clamp(Integer.parseInt(Files.readString(f).trim()));
                        } catch (Exception ignored) {}
                }
                return value;
        }

        public static synchronized void set(int v) {
                value = clamp(v);
                loaded = true;
                try {
                        Files.writeString(file(), Integer.toString(value));
                } catch (Exception ignored) {}
        }

        /** Urutan: Auto -> 1 -> 2 -> 3 -> 4 -> 5 -> Auto. */
        public static int next() {
                return (get() + 1) % (MAX + 1);
        }

        public static String label() {
                int v = get();
                return "Scale: " + (v == 0 ? "Auto" : String.valueOf(v));
        }

        /** Terapkan scale baru ke window yang sedang jalan. */
        public static void apply() {
                MinecraftClient.getInstance().onResolutionChanged();
        }
}
