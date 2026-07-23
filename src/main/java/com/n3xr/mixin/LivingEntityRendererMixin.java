package com.n3xr.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.n3xr.N3XRConfig;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

	@Inject(method = "render", at = @At("HEAD"))
	private void n3xr$beforeRender(S state, net.minecraft.client.util.math.MatrixStack matrices,
	                                net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (N3XRConfig.hitColorEnabled && ((LivingEntityRendererMixinAccessor) state).n3xr$isHurt()) {
			int color = N3XRConfig.hitColor;
			float r = ((color >> 16) & 0xFF) / 255f;
			float g = ((color >> 8) & 0xFF) / 255f;
			float b = (color & 0xFF) / 255f;
			RenderSystem.setShaderColor(r, g, b, 1.0f);
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void n3xr$afterRender(S state, net.minecraft.client.util.math.MatrixStack matrices,
	                                net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}
  }
