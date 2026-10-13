package com.n3xr.nametag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

/**
 * Tag sosial (YouTube, TikTok, dll) yang tampil di samping icon N3XR di nametag.
 * Pilihan disimpan di config/n3xr-nametag.txt (satu baris: key tag, kosong = tanpa tag).
 *
 * Catatan: tag cuma tampil di layar sendiri. Pemain lain nggak bisa melihatnya,
 * karena sinkronisasi antar pengguna N3XR cuma lewat satu bit skin-settings
 * (lihat N3XRUsers) yang nggak muat buat data tag.
 */
public final class N3XRTags {

        public record Tag(String key, String label, int color, char icon) {}

        /** Urutan = urutan tampil di menu. Boleh ditambah di akhir. */
        public static final List<Tag> ALL = List.of(
                new Tag("youtube", "YouTube", 0xFF3B3B, '\uE001'),
                new Tag("tiktok", "TikTok", 0x25F4EE, '\uE002'),
                new Tag("twitch", "Twitch", 0xA970FF, '\uE003'),
                new Tag("instagram", "Instagram", 0xE1306C, '\uE004'),
                new Tag("discord", "Discord", 0x7289DA, '\uE005'),
                new Tag("kick", "Kick", 0x53FC18, '\uE006'),
                new Tag("x", "X", 0xFFFFFF, '\uE007'),
                new Tag("facebook", "Facebook", 0x4A90FF, '\uE008')
        );

        /** Font bitmap ikon tag (assets/n3xr/font/tags.json + textures/font/tags.png). */
        private static final Identifier TAG_FONT = Identifier.of("n3xr", "tags");

        private static String selected = null;
        private static boolean loaded = false;

        private N3XRTags() {}

        private static Path file() {
                return FabricLoader.getInstance().getConfigDir().resolve("n3xr-nametag.txt");
        }

        public static Tag find(String key) {
                if (key == null) return null;
                for (Tag t : ALL) if (t.key().equals(key)) return t;
                return null;
        }

        public static synchronized String getSelectedKey() {
                if (!loaded) {
                        loaded = true;
                        try {
                                Path f = file();
                                if (Files.exists(f)) {
                                        String k = Files.readString(f).trim();
                                        selected = find(k) != null ? k : null;
                                }
                        } catch (Exception ignored) {}
                }
                return selected;
        }

        /** null = tanpa tag. */
        public static synchronized void setSelectedKey(String key) {
                selected = find(key) != null ? key : null;
                loaded = true;
                try {
                        Files.writeString(file(), selected == null ? "" : selected);
                } catch (Exception ignored) {}
        }

        /** Teks tag berwarna (bold), atau null kalau tidak ada tag dipilih. */
        public static Text selectedTagText() {
                Tag t = find(getSelectedKey());
                return t == null ? null : tagText(t);
        }

        /** Ikon pixel-art tag. Warna teks putih supaya warna asli ikon tidak ter-tint. */
        public static MutableText iconText(Tag t) {
                return Text.literal(String.valueOf(t.icon()))
                        .styled(s -> s.withFont(TAG_FONT).withColor(TextColor.fromRgb(0xFFFFFF)));
        }

        /** [ikon] [nama tag] -- ikon bawaan warna sendiri, nama tag bold warna brand. */
        public static MutableText tagText(Tag t) {
                MutableText label = Text.literal(t.label())
                        .styled(s -> s.withColor(TextColor.fromRgb(t.color() & 0xFFFFFF)).withBold(true));
                return Text.empty().append(iconText(t)).append(" ").append(label);
        }
}
