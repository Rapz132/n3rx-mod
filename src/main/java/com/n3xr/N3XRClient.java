package com.n3xr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;

public class N3XRClient implements ClientModInitializer {

	private static KeyBinding openSettingsKey;
	private final ArrayDeque<Long> clickTimes = new ArrayDeque<>();
	private boolean wasAttackPressed = false;

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
					client.setScreen(new N3XRHudEditScreen());
				}
			}

			boolean pressed = client.options.attackKey.isPressed();
			if (pressed && !wasAttackPressed) {
				clickTimes.addLast(System.currentTimeMillis());
			}
			wasAttackPressed = pressed;

			long now = System.currentTimeMillis();
			while (!clickTimes.isEmpty() && now - clickTimes.peekFirst() > 1000) {
				clickTimes.pollFirst();
			}

			if (client.player != null && N3XRConfig.nightVisionEnabled) {
				var current = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
				if (current == null || current.getDuration() < 20) {
					client.player.addStatusEffect(new StatusEffectInstance(
						StatusEffects.NIGHT_VISION, 999999, 0, true, false, false
					));
				}
			}
		});

		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null || mc.options.hudHidden) return;

			if (N3XRConfig.showFps) renderFps(context, mc);
			if (N3XRConfig.showArmor) renderArmorHud(context, mc);
			if (N3XRConfig.showCps) renderCps(context, mc);
			if (N3XRConfig.showPing) renderPing(context, mc);
			if (N3XRConfig.showKeystrokes) renderKeystrokes(context, mc);
		});
	}

	private void renderFps(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		context.drawText(mc.textRenderer, Text.literal("FPS: " + mc.getCurrentFps()),
			N3XRConfig.fpsX, N3XRConfig.fpsY, N3XRConfig.fpsColor, true);
	}

	private void renderArmorHud(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		int y = N3XRConfig.armorY;
		for (ItemStack armor : mc.player.getArmorItems()) {
			if (!armor.isEmpty()) {
				context.drawItem(armor, N3XRConfig.armorX, y);
				context.drawItemInSlot(mc.textRenderer, armor, N3XRConfig.armorX, y);
			} else {
				context.fill(N3XRConfig.armorX, y, N3XRConfig.armorX + 16, y + 16, 0x40FFFFFF);
			}
			y += 20;
		}
	}

	private void renderCps(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		context.drawText(mc.textRenderer, Text.literal("CPS: " + clickTimes.size()),
			N3XRConfig.cpsX, N3XRConfig.cpsY, N3XRConfig.cpsColor, true);
	}

	private void renderPing(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		int ping = 0;
		if (mc.getNetworkHandler() != null) {
			PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
			if (entry != null) ping = entry.getLatency();
		}
		context.drawText(mc.textRenderer, Text.literal("Ping: " + ping + "ms"),
			N3XRConfig.pingX, N3XRConfig.pingY, N3XRConfig.pingColor, true);
	}

	private void renderKeystrokes(net.minecraft.client.gui.DrawContext context, MinecraftClient mc) {
		int x = N3XRConfig.keysX;
		int y = N3XRConfig.keysY;
		int size = 18;
		int gap = 2;

		boolean w = mc.options.forwardKey.isPressed();
		boolean a = mc.options.leftKey.isPressed();
		boolean s = mc.options.backKey.isPressed();
		boolean d = mc.options.rightKey.isPressed();

		drawKey(context, mc, "W", x + size + gap, y, size, w);
		drawKey(context, mc, "A", x, y + size + gap, size, a);
		drawKey(context, mc, "S", x + size + gap, y + size + gap, size, s);
		drawKey(context, mc, "D", x + (size + gap) * 2, y + size + gap, size, d);
	}

	private void drawKey(net.minecraft.client.gui.DrawContext context, MinecraftClient mc, String label, int x, int y, int size, boolean active) {
		int bg = active ? 0xC0FF3333 : 0x80000000;
		context.fill(x, y, x + size, y + size, bg);
		context.fill(x, y, x + size, y + 1, 0xFFFFFFFF);
		context.fill(x, y + size - 1, x + size, y + size, 0xFFFFFFFF);
		context.fill(x, y, x + 1, y + size, 0xFFFFFFFF);
		context.fill(x + size - 1, y, x + size, y + size, 0xFFFFFFFF);

		int tw = mc.textRenderer.getWidth(label);
		context.drawText(mc.textRenderer, label, x + (size - tw) / 2, y + (size - 8) / 2, 0xFFFFFFFF, true);
	}
}
