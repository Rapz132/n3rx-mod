package com.n3xr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;

public class N3XRClient implements ClientModInitializer {

	private static KeyBinding openSettingsKey;
	private final ArrayDeque<Long> clickTimes = new ArrayDeque<>();
	private boolean wasSwinging = false;

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

			if (client.player != null) {
				boolean swinging = client.player.handSwinging;
				if (swinging && !wasSwinging) {
					clickTimes.addLast(System.currentTimeMillis());
				}
				wasSwinging = swinging;
			}

			long now = System.currentTimeMillis();
			while (!clickTimes.isEmpty() && now - clickTimes.peekFirst() > 1000) {
				clickTimes.pollFirst();
			}
		});

		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null || mc.options.hudHidden) return;

			if (N3XRConfig.showFps) renderFps(context, mc);
			if (N3XRConfig.showArmor) renderArmorHud(context, mc);
			if (N3XRConfig.showCps) renderCps(context, mc);
			if (N3XRConfig.showPing) renderPing(context, mc);
		});
	}

	private void renderFps(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		context.drawText(mc.textRenderer, Text.literal("FPS: " + mc.getCurrentFps()),
			N3XRConfig.fpsX, N3XRConfig.fpsY, N3XRConfig.fpsColor(), true);
	}

	private void renderArmorHud(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		int y = N3XRConfig.armorY;
		for (ItemStack armor : mc.player.getArmorItems()) {
			if (!armor.isEmpty()) {
				context.drawItem(armor, N3XRConfig.armorX, y);
				context.drawItemInSlot(mc.textRenderer, armor, N3XRConfig.armorX, y);
				y += 20;
			}
		}
	}

	private void renderCps(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		context.drawText(mc.textRenderer, Text.literal("CPS: " + clickTimes.size()),
			N3XRConfig.cpsX, N3XRConfig.cpsY, N3XRConfig.cpsColor(), true);
	}

	private void renderPing(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		int ping = 0;
		if (mc.getNetworkHandler() != null) {
			PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
			if (entry != null) ping = entry.getLatency();
		}
		context.drawText(mc.textRenderer, Text.literal("Ping: " + ping + "ms"),
			N3XRConfig.pingX, N3XRConfig.pingY, N3XRConfig.pingColor(), true);
	}
}
