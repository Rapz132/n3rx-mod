package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fitur "Entity Culling Distance": membatasi jarak render entity
 * secara terpisah dari render distance chunk. Entity di luar radius
 * yang ditentukan akan dianggap "tidak perlu dirender" sepenuhnya
 * (bukan sekadar disembunyikan secara visual), mengurangi beban
 * render nyata untuk area dengan banyak entity/mob.
 *
 * Menargetkan EntityRenderer.shouldRender, method publik yang ada
 * di semua jenis entity renderer (player, mob, item, dll), sehingga
 * satu mixin ini mencakup semua entity sekaligus.
 *
 * require = 0 supaya jika signature berubah di versi lain, hanya
 * fitur ini yang tidak aktif tanpa menjatuhkan seluruh mod.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityCullingMixin {

        @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true, require = 0)
        private void n3xr$cullByDistance(
                Entity entity,
                Frustum frustum,
                double x,
                double y,
                double z,
                CallbackInfoReturnable<Boolean> cir
        ) {
                if (!N3XRConfig.entityCullingEnabled) return;

                if (!cir.getReturnValueZ()) return;

                double dx = entity.getX() - x;
                double dy = entity.getY() - y;
                double dz = entity.getZ() - z;
                double distanceSquared = dx * dx + dy * dy + dz * dz;

                double maxDistance = N3XRConfig.entityCullingDistance;

                if (distanceSquared > maxDistance * maxDistance) {
                        cir.setReturnValue(false);
                }
        }
}
