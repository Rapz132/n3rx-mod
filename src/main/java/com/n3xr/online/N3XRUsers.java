package com.n3xr.online;

import com.n3xr.N3XRConfig;
import com.n3xr.mixin.N3XRPlayerAccessor;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

/**
 * Saling kenal sesama pengguna N3XR TANPA server / database di internet.
 *
 * Idenya: client Minecraft selalu ngirim "pengaturan skin" ke server (bit-bit
 * apakah cape, jacket, hat, dll. ditampilkan), dan server menyebarkan byte itu
 * ke semua player lain lewat data entity. Byte itu punya 1 bit yang nggak
 * dipakai vanilla (0x80). Client N3XR menyalakan bit itu (lihat
 * N3XRSyncedOptionsMixin), dan client N3XR lain yang melihat player tersebut
 * tinggal membaca bit itu. Server vanilla cuma meneruskannya, jadi nggak
 * butuh mod di server, nggak ada request tambahan, dan nggak ada data
 * yang dikirim ke pihak ketiga.
 *
 * CAPE COSMETIC: 7 bit sisanya (0x01..0x40) biasanya cuma buat layer skin (jacket, lengan,
 * celana, hat). Kalau skin kita skin DEFAULT (Steve/Alex dkk, yang layer luarnya kosong),
 * bit-bit itu nggak berpengaruh ke tampilan, jadi dipakai buat kirim "nomor cape" bawaan
 * (1..6, 0x7F = tanpa cape). Pemain berskin custom TIDAK mengirim kode (bit-bit layer-nya
 * dibiarkan asli), dan penerima juga mengabaikannya, jadi skin mereka nggak berubah.
 * Cape hasil gambar sendiri tidak bisa dibagikan lewat cara ini (nggak muat di bit).
 *
 * Batas: cuma bisa mengenali player yang lagi dimuat di sekitarmu.
 * Player yang sudah pernah terlihat diingat sampai game ditutup (buat tab list).
 */
public final class N3XRUsers {

        private N3XRUsers() {}

        /** Bit skin-settings yang nggak dipakai vanilla. */
        public static final int FLAG = 0x80;

        private static final int MAX_REMEMBERED = 500;
        private static final Set<String> SEEN = ConcurrentHashMap.newKeySet(); // username huruf kecil
        private static long lastScan = 0;

        /** Nyala = client ini ikut "ngasih tahu" bahwa dia pakai N3XR. */
        public static volatile boolean enabled = true;

        /** Nyala = cape cosmetic pilihanmu ikut dikirim (kalau skin-mu skin default). */
        public static volatile boolean shareCape = true;

        /**
         * Nomor cape = posisi di array ini + 1. APPEND-ONLY: jangan mengubah urutan atau
         * menghapus, karena nomor ini dibaca client N3XR lain (versi mod bisa beda-beda).
         */
        public static final String[] CAPE_KEYS = {
                "brokenheart_cape", "carrot_cape", "cherryblosom_cape",
                "cow_cape", "hearth_cape", "spongebob_cape"
        };

        private static final int NO_CAPE = 0x7F;
        private static volatile boolean ownSkinDefault = false;
        private static volatile int lastSignature = Integer.MIN_VALUE;

        /** Skin ini skin bawaan Minecraft (Steve/Alex dkk)? Skin custom ada di "skins/<hash>". */
        public static boolean isDefaultSkin(AbstractClientPlayerEntity player) {
                try {
                        return player.getSkinTextures().texture().getPath().startsWith("textures/entity/player/");
                } catch (Throwable t) {
                        return false;
                }
        }

        private static int ownCapeCode() {
                String key = N3XRConfig.capeSelectedKey;
                if (key != null) {
                        for (int i = 0; i < CAPE_KEYS.length; i++) {
                                if (CAPE_KEYS[i].equals(key)) return i + 1;
                        }
                }
                return NO_CAPE; // tanpa cape, atau cape gambar sendiri (nggak bisa dibagikan)
        }

        /** Byte pengaturan skin yang dikirim ke server. Dipakai N3XRSyncedOptionsMixin. */
        public static int computeParts(int originalParts) {
                if (shareCape && ownSkinDefault) return FLAG | ownCapeCode();
                return originalParts | FLAG;
        }

        /** Key cape bawaan yang dipakai player lain (kelihatan lewat bit-nya), atau null. */
        public static String capeKeyOf(AbstractClientPlayerEntity player) {
                try {
                        byte parts = player.getDataTracker().get(N3XRPlayerAccessor.n3xr$modelPartsKey());
                        if ((parts & FLAG) == 0) return null;
                        if (!isDefaultSkin(player)) return null; // skin custom: bit lainnya = layer asli
                        int code = parts & 0x7F;
                        return (code >= 1 && code <= CAPE_KEYS.length) ? CAPE_KEYS[code - 1] : null;
                } catch (Throwable t) {
                        return null;
                }
        }

        /** Player ini kelihatan pakai N3XR (bit-nya nyala di data entity)? */
        public static boolean hasFlag(AbstractClientPlayerEntity player) {
                try {
                        byte parts = player.getDataTracker().get(N3XRPlayerAccessor.n3xr$modelPartsKey());
                        return (parts & FLAG) != 0;
                } catch (Throwable t) {
                        return false;
                }
        }

        /** Dipakai tab list: pernah terlihat pakai N3XR (berdasarkan username)? */
        public static boolean isUser(String name) {
                return name != null && SEEN.contains(name.toLowerCase(Locale.ROOT));
        }

        /** Dipanggil tiap client tick (N3XRClient). Murah: scan paling sering 2x per detik. */
        public static void tick(MinecraftClient mc) {
                if (mc.world == null) return;

                // Kirim ulang pengaturan skin kalau kodenya berubah (ganti cape, skin sudah ke-load, ganti akun).
                if (mc.player != null && enabled) {
                        boolean def = isDefaultSkin(mc.player);
                        ownSkinDefault = def;
                        int signature = (shareCape && def) ? ownCapeCode() : -1;
                        if (signature != lastSignature) {
                                lastSignature = signature;
                                try {
                                        mc.options.sendClientSettings();
                                } catch (Throwable ignored) {
                                }
                        }
                }

                long now = System.currentTimeMillis();
                if (now - lastScan < 500) return;
                lastScan = now;

                if (SEEN.size() > MAX_REMEMBERED) SEEN.clear();

                for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
                        if (p == mc.player) continue;
                        String name = p.getGameProfile().getName();
                        if (name == null) continue;
                        String lower = name.toLowerCase(Locale.ROOT);
                        if (hasFlag(p)) SEEN.add(lower);
                        else SEEN.remove(lower);
                }
        }
}
