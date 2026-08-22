package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Fitur "Disable Fog": mendorong jarak mulai/akhir fog sangat jauh
 * sehingga secara efektif tidak terlihat, tanpa membongkar kalkulasi
 * warna/shape fog internal (FogData) yang lebih rawan berubah antar
 * versi. Menargetkan parameter viewDistance pada
 * BackgroundRenderer.applyFog — memperbesar parameter ini sebelum
 * fog dihitung membuat titik awal fog terlalu jauh untuk terlihat
 * dalam jarak render normal.
 *
 * require = 0 supaya jika signature applyFog berbeda di versi lain,
 * hanya fitur ini yang tidak aktif tanpa menjatuhkan seluruh mod.
 */
@Mixin(BackgroundRenderer.class)
public abstract class FogDisableMixin {

        @ModifyVariable(method = "applyFog", at = @At("HEAD"), argsOnly = true, require = 0)
        private static float n3xr$pushFogDistance(
                float viewDistance,
                Camera camera,
                BackgroundRenderer.FogType fogType,
                float skyDarkness,
                boolean thickFog,
                float tickDelta
        ) {
                if (!N3XRConfig.fogDisabledEnabled) return viewDistance;

                return viewDistance * 100.0f;
        }
}
