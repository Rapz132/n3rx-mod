package com.n3xr.nametag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

/**
 * Tag sosial (YouTube, TikTok, dll) yang tampil di samping icon N3XR di nametag.
 * Pilihan disimpan di config/n3xr-nametag.txt (satu baris: key tag, kosong = tanpa tag).
 *
 * Catatan: tag cuma tampil di layar sendiri. Pemain lain nggak bisa melihatnya,
 * karena sinkronisasi antar pengguna N3XR cuma lewat satu bit skin-settings
 * (lihat N3XRUsers) yang nggak muat buat data tag.
 */
public final class N3XRTags {

        public record Tag(String key, String label, int color) {}

        /** Urutan = urutan tampil di menu. Boleh ditambah di akhir. */
        public static final List<Tag> ALL = List.of(
                new Tag("youtube", "YouTube", 0xFF3B3B),
                new Tag("tiktok", "TikTok", 0x25F4EE),
                new Tag("twitch", "Twitch", 0xA970FF),
                new Tag("instagram", "Instagram", 0xE1306C),
                new Tag("discord", "Discord", 0x7289DA),
                new Tag("kick", "Kick", 0x53FC18),
                new Tag("x", "X", 0xFFFFFF),
                new Tag("facebook", "Facebook", 0x4A90FF)
        );

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

        public static MutableText tagText(Tag t) {
                return Text.literal(t.label())
                        .styled(s -> s.withColor(TextColor.fromRgb(t.color() & 0xFFFFFF)).withBold(true));
        }
}
