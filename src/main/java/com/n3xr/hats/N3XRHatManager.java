package com.n3xr.hats;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Registry hat cosmetic N3XR — mirip pola N3XRCapeManager, tapi beda
 * satu hal penting: cape semuanya berbagi SATU bentuk geometri yang
 * sama (cuma beda texture), sedangkan tiap hat punya BENTUK sendiri
 * (mushroom, top hat, straw hat, dll semua modelnya beda). Makanya
 * tiap entry di sini nyimpen referensi ke geometry builder-nya
 * masing-masing (Supplier<ModelPart>), bukan cuma path texture.
 *
 * Cara menambah hat baru:
 * 1. Bikin class model geometri-nya sendiri di package ini
 *    (contoh: N3XRStrawHatModel.java), niru pola getOrBuildModel().
 * 2. Taruh file PNG texture-nya di:
 *    assets/n3xr/textures/hats/{key}.png
 * 3. (Opsional tapi disaranin) taruh icon 2D buat list pilihan di:
 *    assets/n3xr/textures/hats/icons/{key}.png
 * 4. Tambah satu baris di static block HATS di bawah ini.
 */
public class N3XRHatManager {

        public record HatEntry(String key, String displayName, Supplier<ModelPart> modelSupplier) {}

        private static final List<HatEntry> HATS = new ArrayList<>();

        static {
                HATS.add(new HatEntry("straw_hat", "Straw Hat", N3XRStrawHatModel::getOrBuildModel));
                HATS.add(new HatEntry("witch_hat", "Witch Hat", N3XRWitchHatModel::getOrBuildModel));
                // Tambah entry lain di sini kalau model hat lain udah jadi, contoh:
                // HATS.add(new HatEntry("mushroom_hat", "Mushroom Hat", N3XRMushroomHatModel::getOrBuildModel));
                // HATS.add(new HatEntry("top_hat", "Top Hat", N3XRTopHatModel::getOrBuildModel));
        }

        public static List<HatEntry> getAvailableHats() {
                return HATS;
        }

        public static HatEntry getEntry(String key) {
                if (key == null) return null;
                for (HatEntry entry : HATS) {
                        if (entry.key().equals(key)) return entry;
                }
                return null;
        }

        public static Identifier getTextureFor(String key) {
                if (key == null) return null;
                return Identifier.of("n3xr", "textures/hats/" + key + ".png");
        }

        /**
         * Icon 2D untuk ditampilkan di list pilihan hat (N3XRHatSelectScreen).
         * Terpisah dari texture hat 3D-nya, bebas berupa gambar apa saja.
         */
        public static Identifier getIconFor(String key) {
                if (key == null) return null;
                return Identifier.of("n3xr", "textures/hats/icons/" + key + ".png");
        }

        public static ModelPart getSelectedModel() {
                HatEntry entry = getEntry(com.n3xr.N3XRConfig.hatSelectedKey);
                return entry == null ? null : entry.modelSupplier().get();
        }

        public static Identifier getSelectedTexture() {
                return getTextureFor(com.n3xr.N3XRConfig.hatSelectedKey);
        }
}
