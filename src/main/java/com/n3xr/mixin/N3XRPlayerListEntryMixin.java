package com.n3xr.mixin;

import com.mojang.authlib.GameProfile;
import com.n3xr.nametag.N3XRNameTag;
import com.n3xr.online.N3XRUsers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Icon N3XR (font n3xr:icons, karakter U+E000) di depan nama di tab list:
 * buat diri sendiri, dan buat player lain yang pernah terlihat pakai N3XR
 * (lihat online/N3XRUsers).
 *
 * Selalu return Text yang sudah di-prefix, karena di banyak server vanilla
 * getDisplayName() balik null dan PlayerListHud fallback ke nama polos.
 */
@Mixin(PlayerListEntry.class)
public abstract class N3XRPlayerListEntryMixin {

        @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
        private void n3xr$prefixWithIcon(CallbackInfoReturnable<Text> cir) {
                PlayerListEntry self = (PlayerListEntry) (Object) this;

                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null) return;

                GameProfile profile = self.getProfile();
                if (profile == null) return;

                boolean isSelf = profile.getId().equals(mc.player.getUuid());
                if (!isSelf && !N3XRUsers.isUser(profile.getName())) return;

                Text original = cir.getReturnValue();
                Text base = original != null ? original : Text.literal(profile.getName());
                cir.setReturnValue(N3XRNameTag.withIcon(base));
        }
}
