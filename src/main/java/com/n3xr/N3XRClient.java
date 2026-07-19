package com.n3xr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class N3XRClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null || mc.options.hudHidden) return;

			renderFps(context, mc);
			renderArmorHud(context, mc);
		});
	}

	private void renderFps(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		String fpsText = "FPS: " + mc.getCurrentFps();
		context.drawText(mc.textRenderer, Text.literal(fpsText), 5, 5, 0xFFFFFF, true);
	}

	private void renderArmorHud(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		int y = 20;
		for (ItemStack armor : mc.player.getArmorItems()) {
			if (!armor.isEmpty()) {
				context.drawItem(armor, 5, y);
				context.drawItemInSlot(mc.textRenderer, armor, 5, y);
				y += 20;
			}
		}
	}
}                     
