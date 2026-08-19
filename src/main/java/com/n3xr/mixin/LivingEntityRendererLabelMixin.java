package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererLabelMixin {

    @Inject(
        method = "hasLabel",
        at = @At("RETURN"),
        cancellable = true
    )
    private void n3xr$selfNametag(
        LivingEntity entity,
        CallbackInfoReturnable<Boolean> cir
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null) {
            return;
        }

        if (entity != mc.player) {
            return;
        }

        if (!N3XRConfig.showNameTag) {
            return;
        }

        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            return;
        }

        cir.setReturnValue(true);
    }
}
