package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Fitur "GUI/Inventory Animation Reducer": mematikan efek kilau
 * (enchantment glint) pada item ber-enchant, tanpa mengubah model
 * atau warna item itu sendiri.
 *
 * Menargetkan parameter boolean "glint" pada
 * ItemRenderer.getItemGlintConsumer, method static public yang
 * dipanggil setiap kali item dengan potensi glint akan dirender
 * (baik di dunia maupun di GUI/inventory). Memaksa parameter ini
 * selalu false membuat lapisan render kilau dilewati sepenuhnya.
 *
 * require = 0 supaya jika signature berbeda di versi lain, hanya
 * fitur ini yang tidak aktif tanpa menjatuhkan seluruh mod.
 */
@Mixin(ItemRenderer.class)
public abstract class NoGlintMixin {

        @ModifyVariable(method = "getItemGlintConsumer", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
        private static boolean n3xr$disableGlint(boolean glint) {
                return N3XRConfig.noGlintEnabled ? false : glint;
        }
}
