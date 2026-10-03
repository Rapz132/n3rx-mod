package com.n3xr.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nampilin icon N3XR (lewat custom bitmap font, char U+E000, lihat
 * assets/n3xr/font/icons.json) di depan nama SENDIRI doang di tab
 * list -- mod ini client-side murni, jadi nggak ada cara valid
 * buat tau player lain juga pakai N3XR apa nggak tanpa dukungan
 * server (lihat penjelasan sebelumnya).
 *
 * Selalu return Text yang udah di-prefix (nggak cuma nyisipin kalau
 * ada displayName existing), soalnya di banyak server vanilla
 * getDisplayName() balikin null (nggak ada scoreboard team
 * prefix/suffix), dan PlayerListHud fallback ke nama polos dari
 * profile -- kalau kita cuma modif waktu non-null, makanya nggak
 * akan pernah kelihatan efeknya di server kayak gitu.
 */
@Mixin(PlayerListEntry.class)
public abstract class N3XRPlayerListEntryMixin {

        @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
        private void n3xr$prefixOwnNameWithIcon(CallbackInfoReturnable<Text> cir) {
                PlayerListEntry self = (PlayerListEntry) (Object) this;

                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null) return;
                if (self.getProfile() == null) return;
                if (!self.getProfile().getId().equals(mc.player.getUuid())) return;

                Text original = cir.getReturnValue();
                Text base = original != null ? original : Text.literal(self.getProfile().getName());

                MutableText icon = Text.literal("\uE000")
                        .styled(s -> s.withFont(Identifier.of("n3xr", "icons")));

                // Icon & nama di-append sebagai SIBLING di bawah root kosong
                // netral -- BUKAN "icon.append(base)" (itu bikin base jadi
                // child icon, ikut warisin font custom icon, bikin semua
                // huruf nama ilang jadi kotak "missing glyph").
                MutableText prefixed = Text.literal("").append(icon).append(" ").append(base);
                cir.setReturnValue(prefixed);
        }
}
