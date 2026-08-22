package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.render.block.BlockModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Fitur "No Shading" untuk blocks (mirip mod Simply No Shading).
 *
 * Menargetkan BlockModelRenderer.renderQuad, method private yang
 * menerima brightness0-3 sebagai parameter brightness per-vertex
 * dari tiap quad/sisi block. Dengan memaksa keempatnya jadi 1.0f
 * di awal method (LocalCapture), ambient-occlusion shading dari
 * smooth lighting hilang dan block terlihat rata/flat.
 *
 * PERINGATAN: target method ini private dan spesifik untuk yarn
 * 1.21+build.2. Jika signature berubah di versi lain, mixin ini
 * bisa gagal apply saat build — karena itu didaftarkan sebagai
 * optional (required: 0) di mixin config, supaya kegagalannya
 * tidak menjatuhkan seluruh mod, hanya fitur ini yang tidak aktif.
 */
@Mixin(BlockModelRenderer.class)
public abstract class NoBlockShadingMixin {

        @ModifyVariable(method = "renderQuad", at = @At("HEAD"), argsOnly = true, ordinal = 0)
        private float n3xr$flattenBrightness0(float brightness) {
                return N3XRConfig.noBlockShadingEnabled ? 1.0f : brightness;
        }

        @ModifyVariable(method = "renderQuad", at = @At("HEAD"), argsOnly = true, ordinal = 1)
        private float n3xr$flattenBrightness1(float brightness) {
                return N3XRConfig.noBlockShadingEnabled ? 1.0f : brightness;
        }

        @ModifyVariable(method = "renderQuad", at = @At("HEAD"), argsOnly = true, ordinal = 2)
        private float n3xr$flattenBrightness2(float brightness) {
                return N3XRConfig.noBlockShadingEnabled ? 1.0f : brightness;
        }

        @ModifyVariable(method = "renderQuad", at = @At("HEAD"), argsOnly = true, ordinal = 3)
        private float n3xr$flattenBrightness3(float brightness) {
                return N3XRConfig.noBlockShadingEnabled ? 1.0f : brightness;
        }
}
