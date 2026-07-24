package com.n3xr.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.n3xr.N3XRConfig;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

	@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
	private void n3xr$before(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		int hurtTime = ((LivingEntityAccessor) entity).n3xr$getHurtTime();
		if (N3XRConfig.hitColorEnabled && hurtTime > 0) {
			int color = N3XRConfig.hitColor;
			float r = ((color >> 16) & 0xFF) / 255f;
			float g = ((color >> 8) & 0xFF) / 255f;
			float b = (color & 0xFF) / 255f;
			RenderSystem.setShaderColor(r, g, b, 1.0f);
		}
	}

	@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("RETURN"))
	private void n3xr$after(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
			immediate.draw();
		}
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}
}
