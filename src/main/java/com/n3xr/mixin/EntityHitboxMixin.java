package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.render.entity.EntityRenderer.class)
public class EntityHitboxMixin {

	@Inject(method = "render", at = @At("TAIL"))
	private void n3xr$drawHitbox(Entity entity, float yaw, float tickDelta, MatrixStack matrices,
	                              VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (!N3XRConfig.hitboxEnabled) return;
		if (!(entity instanceof LivingEntity)) return;
		if (entity == MinecraftClient.getInstance().player) return;

		Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
		int color = N3XRConfig.hitboxColor;
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;

		net.minecraft.client.render.VertexConsumer buffer = vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines());
		WorldRenderer.drawBox(matrices, buffer, box, r, g, b, 1.0f);
	}
          }
