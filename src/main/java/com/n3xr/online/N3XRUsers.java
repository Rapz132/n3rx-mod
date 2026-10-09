package com.n3xr.online;

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
