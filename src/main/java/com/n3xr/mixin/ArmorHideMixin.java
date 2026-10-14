package com.n3xr.mixin;

import com.n3xr.N3XRArmorHide;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skip render armor (termasuk trim) untuk slot yang disembunyikan,
 * khusus player sendiri. Armor di inventory tetap utuh dan tetap
 * melindungi; ini cuma soal tampilan. Paper doll di menu cosmetic
 * ikut berubah karena memakai entity player yang sama.
 */
@Mixin(ArmorFeatureRenderer.class)
public class ArmorHideMixin {

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true, require = 0)
    private void n3xr$hideArmor(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            LivingEntity entity,
            EquipmentSlot slot,
            int light,
            BipedEntityModel<?> model,
            CallbackInfo ci
    ) {
        if (entity != MinecraftClient.getInstance().player) return;
        if (N3XRArmorHide.isHidden(slot)) ci.cancel();
    }
}
