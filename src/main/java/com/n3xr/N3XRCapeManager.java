
package com.n3xr.cosmetic;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry cape lokal — bukan fetch dari internet. Semua cape
 * dibundling langsung sebagai file PNG di dalam mod (tidak
 * bergantung akun premium/cracked, tidak butuh koneksi internet).
 *
 * Cara menambah cape baru:
 * 1. Taruh file PNG (32x32, format cape standar Minecraft) di:
 *    assets/n3xr/textures/capes/{key}.png
 * 2. Tambah satu baris di REGISTER_CAPES di bawah ini dengan
 *    key yang sama persis dan nama tampilan yang diinginkan.
 */
public class N3XRCapeManager {

        public record CapeEntry(String key, String displayName) {}

        private static final List<CapeEntry> CAPES = new ArrayList<>();

        static {
                // Contoh entri — ganti/tambah sesuai file PNG yang sudah ditaruh.
                CAPES.add(new CapeEntry("brokenheart_cape", "Broken Heart Cape"));
                CAPES.add(new CapeEntry("carrot_cape", "Carrot Cape"));
                CAPES.add(new CapeEntry("cherryblosom_cape", "Cherry Blossom Cape"));
                CAPES.add(new CapeEntry("cow_cape", "Cow Cape"));
                CAPES.add(new CapeEntry("hearth_cape", "Hearth Cape"));
                CAPES.add(new CapeEntry("spongebob_cape", "Spongebob Cape"));
        }

        public static List<CapeEntry> getAvailableCapes() {
                return CAPES;
        }

        public static Identifier getTextureFor(String key) {
                if (key == null) return null;
                return Identifier.of("n3xr", "textures/capes/" + key + ".png");
        }

        /**
         * Icon 2D untuk ditampilkan di list pilihan cape (N3XRCapeSelectScreen).
         * Terpisah dari texture cape 3D-nya (yang harus format UV 64x32) —
         * icon ini bebas berupa gambar apa saja, misalnya siluet/artwork
         * konsep dari cape tersebut, karena cuma digambar datar di UI.
         */
        public static Identifier getIconFor(String key) {
                if (key == null) return null;
                return Identifier.of("n3xr", "textures/capes/icons/" + key + ".png");
        }

        public static Identifier getSelectedTexture() {
                return getTextureFor(com.n3xr.N3XRConfig.capeSelectedKey);
        }
}
