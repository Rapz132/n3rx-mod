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
 * Cape gambar sendiri. Kanvasnya 10x16 (sisi luar cape), disimpan sebagai
 * PNG 64x32 format cape standar di config/n3xr_custom_cape.png.
 *
 * Texture-nya didaftarkan langsung ke TextureManager dengan id
 * n3xr:textures/capes/custom.png, jadi semua kode cape yang sudah ada
 * (N3XRCapeManager.getTextureFor, N3XRCapeFeatureMixin) otomatis bisa
 * memakainya cuma dengan key "custom", tanpa file PNG di dalam mod.
 */
public final class N3XRCustomCape {

        private N3XRCustomCape() {}

        public static final String KEY = "custom";
        public static final int W = 10;
        public static final int H = 16;

        private static final Identifier TEXTURE_ID = Identifier.of("n3xr", "textures/capes/custom.png");
        private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("n3xr_custom_cape.png");

        /** ARGB, 0 = transparan. Isi sisi luar cape (10x16). */
        private static final int[] PIXELS = new int[W * H];
        private static boolean loaded = false;
        private static boolean registered = false;

        public static int[] getPixelsCopy() {
                load();
                return PIXELS.clone();
        }

        public static void setPixels(int[] src) {
                System.arraycopy(src, 0, PIXELS, 0, Math.min(src.length, PIXELS.length));
        }

        /** Dipanggil lazy dari N3XRCapeManager.getTextureFor saat key == "custom". */
        public static void ensureRegistered() {
                if (registered) return;
                load();
                register();
        }

        /** Simpan ke PNG + daftarkan ulang texture. Return false kalau gagal nulis file. */
        public static boolean save() {
                load();
                NativeImage image = build();
                boolean ok = true;
                try {
                        image.writeTo(FILE);
                } catch (IOException e) {
                        e.printStackTrace();
                        ok = false;
                }
                // build() baru, karena image di atas dimiliki texture setelah didaftarkan
                register();
                image.close();
                return ok;
        }

        private static void load() {
                if (loaded) return;
                loaded = true;
                if (!Files.exists(FILE)) return;
                try (InputStream in = Files.newInputStream(FILE); NativeImage image = NativeImage.read(in)) {
                        if (image.getWidth() >= 11 && image.getHeight() >= 17) {
                                for (int y = 0; y < H; y++) {
                                        for (int x = 0; x < W; x++) {
                                                PIXELS[y * W + x] = abgrToArgb(image.getColor(1 + x, 1 + y));
                                        }
                                }
                        }
                } catch (IOException | RuntimeException e) {
                        e.printStackTrace();
                }
        }

        private static void register() {
                NativeImage image = build();
                MinecraftClient.getInstance().getTextureManager()
                        .registerTexture(TEXTURE_ID, new NativeImageBackedTexture(image));
                registered = true;
        }

        /**
         * Susun texture cape 64x32 dari kanvas 10x16. Layout UV cuboid cape (10x16x1 di uv 0,0):
         * atas (1,0) 10x1 | bawah (11,0) 10x1 | kiri (0,1) 1x16 | luar (1,1) 10x16 |
         * kanan (11,1) 1x16 | dalam (12,1) 10x16.
         */
        private static NativeImage build() {
                NativeImage img = new NativeImage(64, 32, true);
                for (int y = 0; y < 32; y++) for (int x = 0; x < 64; x++) img.setColor(x, y, 0);

                for (int y = 0; y < H; y++) {
                        for (int x = 0; x < W; x++) {
                                int argb = PIXELS[y * W + x];
                                img.setColor(1 + x, 1 + y, argbToAbgr(argb));          // sisi luar
                                img.setColor(12 + (W - 1 - x), 1 + y, argbToAbgr(argb)); // sisi dalam (dicermin)
                        }
                }
                for (int x = 0; x < W; x++) {
                        img.setColor(1 + x, 0, argbToAbgr(PIXELS[x]));                         // atas
                        img.setColor(11 + x, 0, argbToAbgr(PIXELS[(H - 1) * W + x]));          // bawah
                }
                for (int y = 0; y < H; y++) {
                        img.setColor(0, 1 + y, argbToAbgr(PIXELS[y * W]));                     // kiri
                        img.setColor(11, 1 + y, argbToAbgr(PIXELS[y * W + (W - 1)]));          // kanan
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
