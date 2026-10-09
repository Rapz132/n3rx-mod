package com.n3xr.nametag;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

/**
 * Nametag N3XR di atas kepala player sendiri (third person):
 *   [ICON] [USERNAME]
 * Dirender lewat label bawaan Minecraft (lihat mixin/N3XRSelfLabelMixin
 * dan mixin/N3XRNameTagMixin), jadi posisi, background, dan efek sneak
 * sama kayak nametag player lain. Nyala/mati ikut modul "Name Tag".
 */
public final class N3XRNameTag {

        private N3XRNameTag() {}

        private static final Identifier ICON_FONT = Identifier.of("n3xr", "icons");

        /**
         * Diset true begitu mixin ikut ngerender nametag. Renderer lama
         * (N3XRClient.renderWorldNameTag) berhenti kalau ini true, supaya
         * nggak muncul dua nametag. Kalau mixin gagal ke-apply, nilainya
         * tetap false dan renderer lama tetap jalan.
         */
        public static volatile boolean handledByMixin = false;

        /** Aktif kalau modul Name Tag nyala dan lagi third person. */
        public static boolean active(MinecraftClient mc) {
                if (mc.player == null || mc.options == null) return false;
                if (!N3XRConfig.showNameTag) return false;
                return !mc.options.getPerspective().isFirstPerson();
        }

        /** Sisipkan icon N3XR di depan teks apa pun (nametag, tab list). */
        public static Text withIcon(Text base) {
                MutableText icon = Text.literal("\uE000").styled(s -> s.withFont(ICON_FONT));

                // Icon dan nama jadi sibling di root kosong, JANGAN nama di-append ke icon:
                // kalau nggak, nama ikut font icon dan hurufnya jadi kotak.
                return Text.empty().append(icon).append(" ").append(base);
        }

        /** Nametag player sendiri: icon + nama (warna sesuai pengaturan modul Name Tag). */
        public static Text build(PlayerEntity player) {
                MutableText name = player.getDisplayName().copy()
                        .styled(s -> s.withColor(TextColor.fromRgb(N3XRConfig.nameTagColor & 0xFFFFFF)));
                return withIcon(name);
        }
}
