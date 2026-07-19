package com.n3xr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class N3XRClient implements ClientModInitializer {

	private static KeyBinding openSettingsKey;

	@Override
	public void onInitializeClient() {
		openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.n3xr.settings",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_RIGHT_SHIFT,
			"category.n3xr"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openSettingsKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new N3XRConfigScreen());
				}
			}
		});

		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null || mc.options.hudHidden) return;

			if (N3XRConfig.showFps) renderFps(context, mc);
			if (N3XRConfig.showArmor) renderArmorHud(context, mc);
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
