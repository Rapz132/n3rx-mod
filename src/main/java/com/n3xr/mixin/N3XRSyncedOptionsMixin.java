package com.n3xr.mixin;

import com.n3xr.online.N3XRUsers;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nyalakan bit 0x80 di pengaturan skin yang dikirim client ke server, sebagai
 * "tanda" bahwa pemain ini pakai N3XR. Kalau skin-mu skin default, 7 bit lainnya
 * dipakai buat nomor cape cosmetic (lihat N3XRUsers.computeParts). Client N3XR lain membacanya lewat
 * N3XRUsers. Vanilla mengabaikan bit ini, jadi tampilan skin tidak berubah.
 */
@Mixin(GameOptions.class)
public abstract class N3XRSyncedOptionsMixin {

        @Inject(method = "getSyncedOptions", at = @At("RETURN"), cancellable = true, require = 0)
        private void n3xr$markAsN3xr(CallbackInfoReturnable<SyncedClientOptions> cir) {
                if (!N3XRUsers.enabled) return;

                SyncedClientOptions o = cir.getReturnValue();
                cir.setReturnValue(new SyncedClientOptions(
                        o.language(),
                        o.viewDistance(),
                        o.chatVisibility(),
                        o.chatColorsEnabled(),
                        N3XRUsers.computeParts(o.playerModelParts()),
                        o.mainArm(),
                        o.filtersText(),
                        o.allowsServerListing()));
        }
}
