package com.n3xr.customcape;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

/**
 * Cape gambar sendiri dengan 3 resolusi:
 *   scale 1 = "16x"  -> kanvas 10x16,  texture 64x32   (standar Minecraft)
 *   scale 2 = "32x"  -> kanvas 20x32,  texture 128x64
 *   scale 4 = "64x"  -> kanvas 40x64,  texture 256x128
 * (angka 16/32/64 = tinggi cape dalam pixel). Resolusi disimpan lewat ukuran PNG-nya
 * di config/n3xr_custom_cape.png, jadi nggak perlu setting terpisah.
 *
 * Texture didaftarkan langsung ke TextureManager dengan id
 * n3xr:textures/capes/custom.png, jadi semua kode cape yang sudah ada
 * (N3XRCapeManager.getTextureFor, N3XRCapeFeatureMixin) otomatis memakainya
 * lewat key "custom". UV model cape dinormalisasi terhadap 64x32, jadi texture
 * yang lebih besar tampil benar tanpa mengubah model.
 */
public final class N3XRCustomCape {

        private N3XRCustomCape() {}

        public static final String KEY = "custom";
        public static final int[] SCALES = {1, 2, 4};

        private static final Identifier TEXTURE_ID = Identifier.of("n3xr", "textures/capes/custom.png");
        private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("n3xr_custom_cape.png");

        private static int scale = 1;
        /** ARGB, 0 = transparan. Isi sisi luar cape: (10*scale) x (16*scale). */
        private static int[] pixels = starter(1);
        private static boolean loaded = false;
        private static boolean registered = false;

        public static int width(int s) { return 10 * s; }
        public static int height(int s) { return 16 * s; }
        public static String label(int s) { return (16 * s) + "x"; }

        /** Sudah pernah di-Save di editor (file PNG-nya ada)? */
        public static boolean hasSavedFile() {
                return Files.exists(FILE);
        }

        public static synchronized int getScale() {
                load();
                return scale;
        }

        public static synchronized int[] getPixelsCopy() {
                load();
                return pixels.clone();
        }

        /** Ubah ukuran kanvas (nearest-neighbor): dipakai editor waktu ganti 16x/32x/64x. */
        public static int[] resample(int[] src, int srcScale, int dstScale) {
                int sw = width(srcScale), sh = height(srcScale);
                int dw = width(dstScale), dh = height(dstScale);
                int[] out = new int[dw * dh];
                for (int y = 0; y < dh; y++) {
                        for (int x = 0; x < dw; x++) {
                                out[y * dw + x] = src[(y * sh / dh) * sw + (x * sw / dw)];
                        }
                }
                return out;
        }

        /** Desain awal: cape merah dengan bingkai terang (tebal bingkai = scale). */
        public static int[] starter(int s) {
                int w = width(s), h = height(s);
                int body = 0xFFB82C2C, edge = 0xFFFF5555;
                int[] out = new int[w * h];
                for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                                boolean border = x < s || y < s || x >= w - s || y >= h - s;
                                out[y * w + x] = border ? edge : body;
                        }
                }
                return out;
        }

        /** Dipanggil lazy dari N3XRCapeManager.getTextureFor saat key == "custom". */
        public static synchronized void ensureRegistered() {
                if (registered) return;
                load();
                register();
        }

        /** Simpan ke PNG + daftarkan ulang texture. Return false kalau gagal nulis file. */
        public static synchronized boolean save(int newScale, int[] newPixels) {
                load();
                if (newPixels.length != width(newScale) * height(newScale)) return false;
                scale = newScale;
                pixels = newPixels.clone();

                NativeImage image = build(pixels, scale);
                boolean ok = true;
                try {
                        image.writeTo(FILE);
                } catch (IOException e) {
                        e.printStackTrace();
                        ok = false;
                }
                image.close();
                register(); // build() baru: image di atas sudah ditutup
                return ok;
        }

        private static void load() {
                if (loaded) return;
                loaded = true;
                if (!Files.exists(FILE)) return;
                try (InputStream in = Files.newInputStream(FILE); NativeImage image = NativeImage.read(in)) {
                        int s = image.getWidth() / 64;
                        boolean valid = (s == 1 || s == 2 || s == 4)
                                && image.getWidth() == 64 * s && image.getHeight() == 32 * s;
                        if (!valid) return;
                        int w = width(s), h = height(s);
                        int[] loadedPixels = new int[w * h];
                        for (int y = 0; y < h; y++) {
                                for (int x = 0; x < w; x++) {
                                        loadedPixels[y * w + x] = abgrToArgb(image.getColor(s + x, s + y));
                                }
                        }
                        scale = s;
                        pixels = loadedPixels;
                } catch (IOException | RuntimeException e) {
                        e.printStackTrace();
                }
        }

        private static void register() {
                NativeImage image = build(pixels, scale);
                MinecraftClient.getInstance().getTextureManager()
                        .registerTexture(TEXTURE_ID, new NativeImageBackedTexture(image));
                registered = true;
        }

        /**
         * Susun texture cape (64s x 32s) dari kanvas (10s x 16s). Layout UV cuboid cape,
         * semua posisi dikali s: atas (s,0) | bawah (11s,0) | kiri (0,s) | luar (s,s) |
         * kanan (11s,s) | dalam (12s,s). Tebal sisi = s pixel.
         */
        private static NativeImage build(int[] px, int s) {
                int w = width(s), h = height(s);
                NativeImage img = new NativeImage(64 * s, 32 * s, true);
                for (int y = 0; y < 32 * s; y++) for (int x = 0; x < 64 * s; x++) img.setColor(x, y, 0);

                for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                                int c = argbToAbgr(px[y * w + x]);
                                img.setColor(s + x, s + y, c);                    // sisi luar
                                img.setColor(12 * s + (w - 1 - x), s + y, c);     // sisi dalam (dicermin)
                        }
                }
                for (int dy = 0; dy < s; dy++) {
                        for (int x = 0; x < w; x++) {
                                img.setColor(s + x, dy, argbToAbgr(px[x]));                    // atas
                                img.setColor(11 * s + x, dy, argbToAbgr(px[(h - 1) * w + x])); // bawah
                        }
                }
                for (int dx = 0; dx < s; dx++) {
                        for (int y = 0; y < h; y++) {
                                img.setColor(dx, s + y, argbToAbgr(px[y * w]));                     // kiri
                                img.setColor(11 * s + dx, s + y, argbToAbgr(px[y * w + (w - 1)]));  // kanan
                        }
                }
                return img;
        }

        // NativeImage nyimpen warna sebagai ABGR, kita kerja pakai ARGB.
        private static int argbToAbgr(int argb) {
                return (argb & 0xFF00FF00) | ((argb & 0xFF) << 16) | ((argb >> 16) & 0xFF);
        }

        private static int abgrToArgb(int abgr) {
                return argbToAbgr(abgr); // tukar R dan B itu simetris
        }
}
