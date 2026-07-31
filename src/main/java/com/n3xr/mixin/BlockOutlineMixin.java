package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WorldRenderer.class)
public class BlockOutlineMixin {

	@ModifyArgs(method = "drawBlockOutline", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/render/WorldRenderer;drawShapeOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/util/shape/VoxelShape;DDDFFFF)V"),
		require = 1)
	private static void n3xr$colorOutline(Args args) {
		if (!N3XRConfig.blockOutlineEnabled) return;
		int color = N3XRConfig.blockOutlineColor;
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;
		args.set(7, r);
		args.set(8, g);
		args.set(9, b);
	}
}