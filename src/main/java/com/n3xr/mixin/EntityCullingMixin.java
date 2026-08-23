package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fitur "Entity Culling Distance" dan "Item Entity Optimizer":
 * membatasi jarak render entity secara terpisah dari render distance
 * chunk. Entity di luar radius yang ditentukan akan dianggap "tidak
 * perlu dirender" sepenuhnya (bukan sekadar disembunyikan secara
 * visual), mengurangi beban render nyata untuk area dengan banyak
 * entity/mob.
 *
 * ItemEntity (item yang tergeletak/menumpuk di tanah) diberi radius
 * terpisah yang biasanya lebih pendek dari entity_culling_distance
 * umum, karena tumpukan item drop jauh lebih sering menumpuk banyak
 * dan lebih murah untuk di-cull lebih agresif tanpa terasa mengganggu.
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
                if (!cir.getReturnValueZ()) return;

                boolean isItemEntity = entity instanceof ItemEntity;

                double maxDistance;

                if (isItemEntity && N3XRConfig.itemEntityOptimizerEnabled) {
                        maxDistance = N3XRConfig.itemEntityCullingDistance;
                } else if (N3XRConfig.entityCullingEnabled) {
                        maxDistance = N3XRConfig.entityCullingDistance;
                } else {
                        return;
                }

                double dx = entity.getX() - x;
                double dy = entity.getY() - y;
                double dz = entity.getZ() - z;
                double distanceSquared = dx * dx + dy * dy + dz * dz;

                if (distanceSquared > maxDistance * maxDistance) {
                        cir.setReturnValue(false);
                }
        }
}
