package com.n3xr.mixin;

import com.n3xr.totem.N3XRTotemSize;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mengubah ukuran totem yang dipegang (first person). Di awal
 * HeldItemRenderer.renderItem(...) matriks sudah berada di posisi tangan, jadi
 * scale di sini membesar/mengecilkan totem terhadap titik peganggannya, tanpa
 * menggeser tangannya. Selalu push di awal dan pop di akhir supaya matriks tetap
 * seimbang (scale hanya diterapkan kalau itemnya totem).
 */
@Mixin(HeldItemRenderer.class)
public class HeldTotemScaleMixin {

    private static final String RENDER_ITEM =
            "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/client/render/model/json/ModelTransformationMode;Z"
                    + "Lnet/minecraft/client/util/math/MatrixStack;"
                    + "Lnet/minecraft/client/render/VertexConsumerProvider;I)V";

    @Inject(method = RENDER_ITEM, at = @At("HEAD"), require = 0)
    private void n3xr$scaleHeldTotemStart(LivingEntity entity, ItemStack stack, ModelTransformationMode mode,
                                          boolean leftHanded, MatrixStack matrices,
                                          VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.push();
        if (!stack.isOf(Items.TOTEM_OF_UNDYING)) return;

        // mode FIRST_PERSON_RIGHT_HAND = tangan kanan; tangan utama = kanan kalau mainArm RIGHT
        boolean rightHandMode = mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND;
        boolean mainHand = rightHandMode == (entity.getMainArm() == Arm.RIGHT);
        float factor = N3XRTotemSize.heldScale(mainHand);
        if (factor != 1.0f) matrices.scale(factor, factor, factor);
    }

    @Inject(method = RENDER_ITEM, at = @At("RETURN"), require = 0)
    private void n3xr$scaleHeldTotemEnd(LivingEntity entity, ItemStack stack, ModelTransformationMode mode,
                                        boolean leftHanded, MatrixStack matrices,
                                        VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.pop();
    }
}
